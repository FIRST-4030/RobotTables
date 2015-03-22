The underlying NetworkTables implementation doesn't seem to be entirely reliable -- particularly on the cRIO -- lacks several features I'd like, and can be complicated to use correctly even in fairly simple cases.

So here's a proposal for an alternative broadcast robot comm system. I intend to provide a Java reference implementation, as well as a cRIO-compatible Java implementation (which requires use of an alternate socket handler but otherwise matches the reference implementation).

Differences from NetworkTables include per-key incremental updates, support for deletion, completely peer-to-peer networking, a human-readable wire format, mandatory closed-loop monitoring, per-table "administrative" namespace for keys, table discovery, and protection against multiple publishers.

## Message Format
`[ Type ]\0[ Table ]\0[ Key ]\0[ Value ]`

Type is the message type number as noted in the message type list below, represented in ASCII text (for human readability and to avoid being `\0`). Table, Key and Value may contain arbitrary data except for the `\0` (null) character. Valid messages must contain exactly 3 `\0` delimiters and must begin with a known message type number; hosts may discard any non-conforming message without further processing.

## Message Types

### Type 1 -- Table Query
Key is EXISTS when checking for existing table, or PUBLISH when attempting to publish a new table.

When Key is PUBLISH hosts may assume ownership of the named table if 200 ms elapses with no NAK message for the table. If a NAK is received before 200 ms elapses the named table must not be published, and the PublishEnded event must be raised. Hosts must not send any Publish, Delete or Table Update messages for a table until and unless they have successfully assumed ownership of the table.

### Type 2 -- ACK
When an EXISTS Table Query message arrives, hosts should ACK if publishing the named table. Value should be copied from the related incoming request.

After an END Table Update message arrives, if the Table Update process was successful, hosts should ACK with the Key GENERATION_COUNT and the Value from the related key as provided by the publisher.

When a GENERATION_COUNT ACK message arrives, hosts publishing the related table should update their SubscriberStale timer.

### Type 3 -- NAK
When a PUBLISH Table Query message arrives, hosts should NAK if publishing the named table. The Key and Value fields should be copied from the incoming request.

When a Table Update message arrives, hosts should NAK if publishing the named table. The Key and Value fields should be copied from the incoming requests.

If a USER, ADMIN, or END NAK message arrives hosts must immediately stop publishing the related table, convert it to a subscribed table, and raise the PublishEnded event.

### Type 4 -- Publish Administrative Data
Publishes an administrative (i.e. internal) keys/value pair to all subscribers.

### Type 5 -- Delete Administrative Data
When subscribers receive a Delete Administrative Data request, they should remove the related Key from their local administrative data store, or ignore the message if the key does not exists.

### Type 6 -- Publish User Data
Publishes a user key/value pair to all subscribers.

### Type 7 -- Delete User Data
When subscribers receive a Delete User Data request, they should remove the related Key from their local user data store, or ignore the message if the key does not exists.

### Type 8 -- Table Update
Delimits the table update process, wherein all administrative and user key/value pairs are transmitted to subscribers, to allow initial synchronization of the table with new subscribers and to recover from transient communication losses.

The Table Update process begins by monotonically incrementing the GENERATION_COUNT administrative key. Then a Table Update message with Key USER and Value equal to the number of user key/value pairs in the table (as an ASCII int) is sent. Next Publish User Data messages are sent for each user key/value pair in the table. Finally a Table Update message with Key ADMIN Value equal to the number of administrative key/value pairs in the table (as an ASCII int). Next Publish Administrative Data messages are sent for each administrative key/value pair in the table. Finally a Table Update message with Key END and Value equal to the number of all key/value pairs (user and administrative) is sent, ending the update process.

During the Table Update process subscribers must handle each Publish message as they would at any other time, updating the related local store. Subscribers should count the number of keys received, and if the received count matches expected count from Value of both the USER Table Update and ADMIN Table Update messages, consider the update successful. On successful updates subscribers must delete any keys in the local store that were not received from the publisher during the update and reset their PublisherStale timer.

Subscribers must take care not to double-count keys even if the same key is sent multiple times during the Table Update process (but must still process each update message). If a Delete message is received during the Table Update process the process must not be considered successful. Subscribers should continue counting Publish messages for up to 100 ms after the related END Table Update message, or until the expected count has been reached, to allow for consistent updates even given out-of-order packet delivery.

Subscribers may assume a Table Update process is complete when the expected count is reached, after 100 ms with no Publish or Table Update events for the related table, or when the next USER Table Update message is received. Incomplete updates are not themselves an error condition -- they should simply not trigger the events and processes related to a completed Table Update.

### Type 9 -- Request Table Update
When a Request Table Update message arrives, hosts publishing the named table should begin the Table Update process. This allows recently-subscribed hosts to be brought into sync immediately, without waiting for the next scheduled update.

## Required Administrative Data
Publishers must provide, subscribers must process and expose, the following administrative keys:
* GENERATION_COUNT: A monotonically increasing integer
* UPDATE_INTERVAL: Must be between 200 ms and 30,000 ms. Default: 5,000 ms

Other administrative keys may be ignored, if not used for implementation-specific purposes.

## Required Timers

### PublisherStale
Subscribers must trigger the PublisherStale event if 1.7 (was 2.1) times the UPDATE_INTERVAL has elapsed since the last successful Table Update process.

Publishers set the UPDATE_INTERVAL administrative key and must initiate a Table Update process at least as frequently as that interval.

### SubscriberStale
Publishers must trigger the SubscriberStale event if the last Table Update ACK is more than 2 generations out-of-date, or if 1.7 (was 2.1) times the UPDATE_INTERVAL has elapsed since the last valid Table Update ACK.

## Subscriber Events
### AdminChanged(Table, Key)
Triggered when any administrative data in a subscribed table is updated.
### UserChanged(Table, Key)
Triggered when any user data in a subscribed table is updated.
### PublisherStale(Table)
Triggered when a subscribed table has not been updated in the expected interval.

## Publisher Events
### SubscriberStale(Table)
Triggered when a published table has no responding subscribers in the expected interval.
### PublishEnded(Table)
Triggered when a published table is converted to a subscribed table.

## UI Notes
Hosts may wish to track Publish (and/or other) events for non-subscribed tables to provide end users with suggestions for available tables, but end users must be allowed to subscribe to tables even if no related data has been seen recently.

## Networking Notes
RobotTables is intended for use on fairly low-loss, low-latency networks. It can recover from occasional packet loss, but will be unreliable in the face of ongoing packet loss. It will also be unreliable -- or perhaps completely unusable -- on networks with more than 100 ms latency between hosts, or on hosts that cannot service requests within 100 ms. If you have such a network or host, consider scaling the default intervals to match your expected performance.

Messages are sent as broadcast UDP packets and are completely independent. Dropped messages are not detected; the Table Update process is used to re-sync tables periodically. Out-of-order messages are not detected; out-of-order delivery is not relevant (outside the weak state created on the subscriber side during the Table Update process, which has its own accommodation).

The combined length of all fields must not exceed the maximum UDP body length (slightly less than 64kB on most systems), and for best performance should not force the UDP packet to exceed the MTU length of the local network (slightly less than 1500 bytes on most networks). Messages that exceed the maximum UDP packet size will be truncated; if truncated before the last `\0` delimiter messages may be discarded by hosts as non-conforming.

Messages are broadcast, so hosts may receive their own messages. Implementations should take care to avoid processing such messages -- the source IP address should be useful in making such distinctions.

## Threading Notes
In the reference implementation, RobotTables has a dedicated thread for receiving and validating messages from the network, and a separate thread for processing the messages to completion, including related callback events. Processing is intentionally single-threaded; among other things the cRIO target has only one execution unit. A single processing queue also provides insight into the rate at which messages are received vs. processed, and can help detect stuck or long-running callback events. To that end, the reference implementation warns when the received-but-not-processed queue size exceeds 50 messages, and if it exceeds 100 messages, drops messages without processing until the queue is back under 50. These threading behaviors are not required, but are desirable in our usage -- I'd be happy to accept patches that add optional alternative queuing behavior that can be selected at runtime/compile-time (bearing in mind the the cRIO requires squawk-compatible Java).

## API Notes
### Set
`set(String key, String value, boolean user)`
`set(String key, double value, boolean user)`
`set(String key, int value, boolean user)`
`set(String key, boolean value, boolean user)`
`set(String key, Blob value, boolean user)`
`remove(String key, boolean user)`
`clear(boolean user)`

The reference implementation will likely also include methods that assume `user == true`

### Get
`String = get(String key, boolean user)`
`double = getDouble(String key, boolean user, boolean trapParseError)`
`int = getInt(String key, boolean user, boolean trapParseError)`
`boolean = getBoolean(String key, boolean user)`
`Blob = getBlob(String key, boolean user)`
`exists(String key)`

The reference implementation will likely also include methods that assume `user == true` and `trapParseError == true`

### Table Info
`String = name()`
`boolean = isWritable()`

### Subscribe
`RobotTable = subscribe(String table)`
`setAdminChanged(RobotTables.SubscriberEvent callbackClass)`
`setUserChanged(RobotTables.SubscriberEvent callbackClass)`
`setPublisherStale(RobotTables.SubscriberEvent callbackClass)`
`boolean = isPublisherStale()`

### Publish
`RobotTable = publish(String table)`
`setSubscriberStale(RobotTables.SubscriberEvent callbackClass)`
`setPublishEnded(RobotTables.SubscriberEvent callbackClass)`
`setUpdateInterval(int interval)`
`boolean = isSubscriberStale()`
`updateTable()`

### Cleanup
`close()`

### RobotTable.Blob
`new Blob(byte[] data, int length)`
`new Blob(String base64Data)`
`String = toString()`
`byte[] = getBytes()`
`int = getLength()`
