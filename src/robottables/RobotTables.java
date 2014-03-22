package robottables;

import java.io.IOException;
import java.util.Date;
import robottables.network.IO;
import robottables.Dispatch.DistpachEvents;
import robottables.network.Queue;
import robottables.network.Queue.QueueEvents;

public class RobotTables implements DistpachEvents, QueueEvents {

    private Dispatch dispatch;

    public static void main(String[] args) {
        new RobotTables().run();
    }

    public void run() {
        // Message queue between listner and dispatch
        Queue queue = new Queue(this);

        try {
            IO io = new IO();

            // Listen for and queue incoming messages
            io.listen(queue);

            // Dispatch all messages from the queue to ourselves
            dispatch = new Dispatch(queue);
            dispatch.setAllHandlers(this);
            (new Thread(dispatch)).start();

            // Send fake data so we have something to work with in tests
            (new Thread(new Sender(io))).start();
        } catch (IOException ex) {
            System.err.println(ex.toString());
        }
    }

    @Override
    public void queueError(int size, boolean draining, int targetSize) {
        if (!draining) {
            System.err.println("Queue Warning: Large message queue size: " + size);
        } else {
            System.err.println("Queue Error: Drained to size: " + targetSize);
        }
        if (dispatch.currentMessage() == null) {
            System.err.println("\tNo dispatch handler running");
        } else {
            long now = new Date().getTime();
            System.err.println("\tDispatch time: " + (now - dispatch.dispatchTime()) + " ms ago");
            System.err.println("\tDispatch message:\n" + dispatch.currentMessage().displayStr());
        }
    }

    @Override
    public void dispatch(Message msg) {
        
        // Sleep longer than the sender interval to induce a queue overload
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
        }
        
        // Do something with the message
        System.out.println("Handled message:\n" + msg.displayStr());
    }

    private class Sender implements Runnable {

        private final IO io;

        public Sender(IO io) {
            this.io = io;
        }

        @Override
        public void run() {
            int i = 0;
            while (true) {
                Message msg = new Message(Message.Type.ACK, "TestTable", "SampleKey", String.valueOf(i));
                try {
                    io.send(msg.toString());
                } catch (IOException ex) {
                    System.err.println(ex.toString());
                }
                i++;
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                }
            }
        }
    }
}
