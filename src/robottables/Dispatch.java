package robottables;

import robottables.Message.Type;
import robottables.network.Queue;

public class Dispatch implements Runnable {

    private final Queue queue;
    private Message msg;
    private long timestamp;
    private final DistpachEvents[] handlers = new DistpachEvents[Type.HIGHEST + 1];

    public Dispatch(Queue queue) {
        this.queue = queue;
        setAllHandlers(null);
    }

    public void setHandler(DistpachEvents handler, int type) {
        if (type < Type.LOWEST || type > Type.HIGHEST) {
            throw new IllegalArgumentException("Invalid type: " + type);
        }
        handlers[type] = handler;
    }

    public void setAllHandlers(DistpachEvents handler) {
        for (int i = Type.LOWEST; i < Type.HIGHEST; i++) {
            setHandler(handler, i);
        }
    }

    public Message currentMessage() {
        return msg;
    }

    public long dispatchTime() {
        return timestamp;
    }

    @Override
    public void run() {
        while (true) {
            // Wait for the next message
            msg = queue.get();

            // Dispatch if we have a handler
            if (handlers[msg.getType()] != null) {
                timestamp = System.currentTimeMillis();
                handlers[msg.getType()].dispatch(msg);
            } else {
                System.err.println("Unhandled message:\n" + msg.displayStr());
            }

            // Reset the msg pointer
            msg = null;
        }
    }

    public interface DistpachEvents {

        public void dispatch(Message msg);
    }
}
