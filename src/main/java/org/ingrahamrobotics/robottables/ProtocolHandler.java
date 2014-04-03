package org.ingrahamrobotics.robottables;

import java.util.Hashtable;
import org.ingrahamrobotics.robottables.interfaces.InternalTableHandler;
import org.ingrahamrobotics.robottables.interfaces.RobotProtocol;

// TODO: Implement
public class ProtocolHandler implements RobotProtocol {

    public void sendPublishRequest(final String tableName) {

    }

    public void sendFullUpdate(final String tableName, final Hashtable tableValues) {

    }

    public void sendKeyUpdate(final String tableName, final String keyName, final String keyValue) {

    }

    public void sendKeyDelete(final String tableName, final String keyName) {

    }

    public void setInternalHandler(final InternalTableHandler handler) {

    }

    public void dispatch(final Message msg) {
        // Sleep longer than the sender interval to induce a queue overload
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
        }

        // Do something with the message
        System.out.println("Handled message:\n" + msg.displayStr());
    }
}
