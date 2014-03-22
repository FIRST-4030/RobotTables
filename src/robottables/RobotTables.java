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
                io.send("FakeData-" + i);
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
        System.out.println("Recv: " + data);
    }

    @Override
    public void error(String err) {
        System.err.println("Err: " + err);
    }
}
