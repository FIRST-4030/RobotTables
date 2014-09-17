package org.ingrahamrobotics.robottables.interfaces;

public interface ProtocolTable {

    public void setReadyToPublish(boolean readyToPublish);

    public boolean isReadyToPublish();

    public void updatedNow();
}
