package org.ingrahamrobotics.robottables.interfaces;

import java.util.Hashtable;
import static org.ingrahamrobotics.robottables.Dispatch.DistpachEvents;

public interface RobotProtocol extends DistpachEvents {

    public void sendPublishRequest(String tableName);

    public void sendFullUpdate(String tableName, Hashtable tableValues);

    public void sendKeyUpdate(String tableName, String keyName, String keyValue);

    public void sendKeyDelete(String tableName, String keyName);

    public void setInternalHandler(InternalTableHandler handler);

    public void sendAdminKeyUpdate(String name, String key, String value);

    public void sendAdminKeyDelete(String name, String key);
}