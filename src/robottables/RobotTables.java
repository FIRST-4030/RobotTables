package robottables;

import java.io.IOException;
import robottables.network.IO;

public class RobotTables implements IO.ListenEvents {

    public static void main(String[] args) {
        new RobotTables().run();
    }

    public void run() {
        try {
            IO io = new IO();
            io.listen(this);

            int i = 0;
            while (true) {
                Message msg = new Message(Message.Type.ACK, "TestTable", "SampleKey", String.valueOf(i));
                io.send(msg.toString());
                i++;
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                }
            }
        } catch (IOException ex) {
            System.err.println(ex.toString());
        }
    }

    @Override
    public void recv(String data) {
        try {
            Message msg = new Message(data);
            System.out.println("Parsed Message: ");
            System.out.println("\tType: " + msg.getType());
            System.out.println("\tTable: " + msg.getTable());
            System.out.println("\tKey: " + msg.getKey());
            System.out.println("\tValue: " + msg.getValue());
        } catch (IllegalArgumentException ex) {
            System.err.println("Unable to parse message: " + ex.toString() + "\n\t" + data);
        }
    }

    @Override
    public void error(String err) {
        System.err.println("Err: " + err);
    }
}
