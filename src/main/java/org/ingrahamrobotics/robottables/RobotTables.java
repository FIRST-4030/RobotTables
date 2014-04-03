package org.ingrahamrobotics.robottables;

import java.io.IOException;
import org.ingrahamrobotics.robottables.Dispatch.DistpachEvents;
import org.ingrahamrobotics.robottables.network.IO;
import org.ingrahamrobotics.robottables.network.Queue;
import org.ingrahamrobotics.robottables.network.Queue.QueueEvents;

public class RobotTables implements DistpachEvents, QueueEvents {

    private Dispatch dispatch;
    // Make IO a package-level variable (instead of a local variable) so that it is accessible from the outside
    IO io;

    public void run() {
        // Message queue between listner and dispatch
        Queue queue = new Queue(this);

        try {
            io = new IO();

            // Listen for and queue incoming messages
            io.listen(queue);

            // Dispatch all messages from the queue to ourselves
            dispatch = new Dispatch(queue);
            dispatch.setAllHandlers(this);
            (new Thread(dispatch)).start();
        } catch (IOException ex) {
            System.err.println(ex.toString());
        }
    }

    public void queueError(int size, boolean draining, int targetSize) {
        if (!draining) {
            System.err.println("Queue Warning: Large message queue size: " + size);
        } else {
            System.err.println("Queue Error: Drained to size: " + targetSize);
        }
        if (dispatch.currentMessage() == null) {
            System.err.println("\tNo dispatch handler running");
        } else {
            long now = System.currentTimeMillis();
            System.err.println("\tDispatch time: " + (now - dispatch.dispatchTime()) + " ms ago");
            System.err.println("\tDispatch message:\n" + dispatch.currentMessage().displayStr());
        }
    }

    public void dispatch(Message msg) {

        // Sleep longer than the sender interval to induce a queue overload
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
        }

        // Do something with the message
        System.out.println("Handled message:\n" + msg.displayStr());
    }
}
