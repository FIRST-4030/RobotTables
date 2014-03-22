package robottables.network;

import robottables.network.desktop.Socket;
import java.io.IOException;

public class IO {

    private static final boolean ON_ROBOT = false;
    private static final String addr = "255.255.255.255";
    private static final int[] ports = new int[]{1130, 1140};

    private ListenEvents eventClass;
    private final Socket[] sockets = new Socket[ports.length];

    public IO() throws IOException {
        for (int i = 0; i < ports.length; i++) {
            sockets[i] = new Socket(addr, ports[i]);
        }
    }

    public void send(String data) throws IOException {
        Socket socket = sockets[0];
        if (ON_ROBOT) {
            socket = sockets[1];
        }
        socket.send(data);
    }

    public void listen(ListenEvents eventClass) {
        this.eventClass = eventClass;
        for (int i = 0; i < ports.length; i++) {
            (new Thread(new RecvThread(sockets[i]))).start();
        }
    }

    public void close() {
        this.eventClass = null;
        for (int i = 0; i < ports.length; i++) {
            sockets[i].close();
        }
    }

    private class RecvThread implements Runnable {

        private final Socket socket;

        public RecvThread(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            boolean done = false;
            while (!done) {
                try {
                    final String message = socket.receive();
                    if (eventClass != null) {
                        eventClass.recv(message);
                    } else {
                        done = true;
                    }
                } catch (IOException ex) {
                    done = true;
                    if (eventClass != null) {
                        eventClass.error(ex.toString());
                    }
                }
            }
        }
    }

    public interface ListenEvents {

        public void recv(String message);

        public void error(String error);
    }
}
