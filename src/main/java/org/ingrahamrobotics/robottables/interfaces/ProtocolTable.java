package org.ingrahamrobotics.robottables.interfaces;

import java.util.Map;
import org.ingrahamrobotics.robottables.api.RobotTable;

public interface ProtocolTable extends RobotTable {

    public void setReadyToPublish(boolean readyToPublish);

    public boolean isReadyToPublish();

    public void updatedNow();

    /**
     * Gets the internal HashMap of user key->value entries.
     *
     * This returns the actual internal map, code that iterates over values should clone/copy the map first.
     */
    public Map<String, String> getUserValues();

    /**
     * Gets the internal HashMap of admin key->value entries.
     *
     * This returns the actual internal map, code that iterates over values should clone/copy the map first.
     */
    public Map<String, String> getAdminValues();
}
