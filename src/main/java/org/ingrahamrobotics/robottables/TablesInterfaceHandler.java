package org.ingrahamrobotics.robottables;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import org.ingrahamrobotics.robottables.api.RobotTable;
import org.ingrahamrobotics.robottables.api.RobotTablesClient;
import org.ingrahamrobotics.robottables.api.TableType;
import org.ingrahamrobotics.robottables.api.listeners.ClientUpdateListener;
import org.ingrahamrobotics.robottables.interfaces.InternalRobotTablesHandler;

public class TablesInterfaceHandler implements RobotTablesClient, InternalRobotTablesHandler, Dispatch.DistpachEvents {

    private Hashtable tableMap = new Hashtable(); // Map from String to InternalTable
    private List listeners = new ArrayList(); // List of ClientUpdateListener
    private Timer tablePublishingTimer = new Timer();

    public void externalPublishedTable(final String tableName) {
        InternalTable airTable = (InternalTable) tableMap.get(tableName);

        if (airTable != null) {
            if (airTable.getType() != TableType.REMOTE) {
                // If we are already publishing the table, clear all values, and change the type
                airTable.internalClear();
                TableType oldType = airTable.getType();
                airTable.setType(TableType.REMOTE);
                fireTableTypeChangeEvent(airTable, oldType, TableType.REMOTE);
            } else if (airTable.getType() == TableType.REMOTE) {
                airTable.internalClear(); // TODO: Should we clear all values when a already remote table is re-published?
                // In fact, what does it even mean that an external table has been published when the table we know about is remote?
            }
        } else {
            // If we don't know about this table, create a new one
            airTable = new InternalTable(this, TableType.REMOTE);
            tableMap.put(tableName, airTable);
            fireNewTableEvent(airTable);
        }
    }

    public void externalKeyUpdated(final String tableName, final String key, final String newValue) {
        // TODO:
        // Assuming that the other client is publishing when we recieve a message from them could lead
        // to a condition where both clients end up thinking that the other is publishing
        InternalTable table = (InternalTable) tableMap.get(tableName);
        if (table == null) {
            externalPublishedTable(tableName);
            table = (InternalTable) tableMap.get(tableName);
        }
        table.internalSet(key, newValue);
    }

    public void externalKeyRemoved(final String tableName, final String key) {
        InternalTable table = (InternalTable) tableMap.get(tableName);
        if (table == null) {
            externalPublishedTable(tableName);
        } else { // We don't care about a key being removed for a table that we have no data on, so put it in an else statement
            table.internalSet(key, null);
        }
    }

    public void internalKeyUpdated(InternalTable table, String key, String newValue) {
    }

    public void internalKeyRemoved(InternalTable table, String key) {
    }

    public void internalTableCleared(InternalTable table) {
    }

    void fireTableTypeChangeEvent(final RobotTable table, final TableType oldType, final TableType newType) {
        for (int i = 0; i < listeners.size(); i++) {
            final ClientUpdateListener listener = (ClientUpdateListener) listeners.get(i);
            listener.onTableChangeType(table, oldType, newType);
        }
    }

    void fireNewTableEvent(final RobotTable table) {
        for (int i = 0; i < listeners.size(); i++) {
            final ClientUpdateListener listener = (ClientUpdateListener) listeners.get(i);
            listener.onNewTable(table);
        }
    }

    public RobotTable getTable(final String tableName) {
        return (InternalTable) tableMap.get(tableName);
    }

    public boolean doesExist(final String tableName) {
        return tableMap.containsKey(tableName);
    }

    public RobotTable publishTable(final String tableName) {
        InternalTable table = (InternalTable) tableMap.get(tableName);
        if (table == null) {
            // If we don't know about this table yet, publish it
            sendPublishRequest(tableName);
            table = new InternalTable(this, TableType.LOCAL);
            tablePublishingTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    InternalTable table = (InternalTable) tableMap.get(tableName);
                    if (table != null) {
                        table.setReadyToPublish(true);
                    }
                    if (table == null) {
                        table = new InternalTable(TablesInterfaceHandler.this, TableType.LOCAL);
                    }
                }
            }, TimeConstants.PUBLISH_WAIT_TIME);
        }
        // Return the table we had before, or published
        return table;
    }

    public void addClientListener(final ClientUpdateListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeClientListener(final ClientUpdateListener listener) {
        listeners.remove(listener);
    }

    // ********************** //
    // *** Communications *** //
    // ********************** //
    private void sendPublishRequest(String tableName) {
    }

    private void sendFullUpdate(String tableName) {
    }

    private void sendKeyUpdate(String tableName, String keyName, String keyValue) {
    }

    private void sendKeyDelete(String tableName, String keyName) {
    }

    public void dispatch(final Message msg) {


    }
}
