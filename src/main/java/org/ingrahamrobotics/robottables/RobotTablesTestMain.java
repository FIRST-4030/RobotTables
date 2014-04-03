package org.ingrahamrobotics.robottables;

import java.io.IOException;
import org.ingrahamrobotics.robottables.network.IO;

public class RobotTablesTestMain {

    public static void main(String[] args) {
        new RobotTablesTestMain().run();
    }

    public void run() {

        RobotTables communications = new RobotTables();

        communications.run(); // This will fork into a new thread and not block
        // Send fake data so we have something to work with in tests
        (new Thread(new Sender(communications.io))).start();
    }

    private class Sender implements Runnable {

        private final IO io;

        public Sender(IO io) {
            this.io = io;
        }

        public void run() {
            for (int i = 0; true; i++) {
                Message msg = new Message(Message.Type.ACK, "TestTable", "SampleKey", String.valueOf(i));
                try {
                    io.send(msg.toString());
                } catch (IOException ex) {
                    System.err.println(ex.toString());
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                }
            }
        }
    }
}
