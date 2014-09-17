package org.ingrahamrobotics.robottables;

import java.io.IOException;
import java.util.Hashtable;
import org.ingrahamrobotics.robottables.interfaces.InternalTableHandler;
import org.ingrahamrobotics.robottables.interfaces.RobotProtocol;
import org.ingrahamrobotics.robottables.network.IO;

public class ProtocolHandler implements RobotProtocol {

    private InternalTableHandler handler;
    private final IO io;

    public ProtocolHandler() throws IOException {
        io = new IO();
    }

    public void sendPublishRequest(final String tableName) {
        sendMessage(new Message(Message.Type.QUERY, tableName, "publish", "_"));
    }

    public void sendFullUpdate(final String tableName, final Hashtable tableValues) {

    }

    public void sendKeyUpdate(final String tableName, final String key, final String value) {
        sendMessage(new Message(Message.Type.PUBLISH_USER, tableName, key, value));
    }

    public void sendKeyDelete(final String tableName, final String keyName) {
        sendMessage(new Message(Message.Type.DELETE_USER, tableName, keyName, "_"));
    }

    public void sendAdminKeyUpdate(final String tableName, final String key, final String value) {
        sendMessage(new Message(Message.Type.PUBLISH_ADMIN, tableName, key, value));
    }

    public void sendAdminKeyDelete(final String tableName, final String key) {
        sendMessage(new Message(Message.Type.DELETE_ADMIN, tableName, key, "_"));
    }

    public void sendMessage(final Message message) {
        try {
            io.send(message.toString());
        } catch (IOException e) {
            System.err.println("Error sending message '" + message.displayStr() + "'.");
            e.printStackTrace();
        }
    }

    public void dispatch(final Message msg) {
        System.out.println("Received message:\n" + msg.displayStr());
    }

    public void setInternalHandler(final InternalTableHandler handler) {
        this.handler = handler;
    }
}
