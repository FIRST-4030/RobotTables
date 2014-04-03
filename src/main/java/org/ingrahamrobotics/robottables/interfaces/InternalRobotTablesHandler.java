package org.ingrahamrobotics.robottables.interfaces;

import org.ingrahamrobotics.robottables.InternalTable;

public interface InternalRobotTablesHandler {

    public void internalKeyUpdated(InternalTable table, String key, String newValue);

    public void internalKeyRemoved(InternalTable table, String key);

    public void internalTableCleared(InternalTable table);

    public void externalPublishedTable(String tableName);

    public void externalKeyUpdated(String tableName, String key, String newValue);

    public void externalKeyRemoved(String tableName, String key);
}