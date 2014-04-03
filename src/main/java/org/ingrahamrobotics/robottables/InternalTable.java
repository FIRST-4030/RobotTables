package org.ingrahamrobotics.robottables;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import org.ingrahamrobotics.robottables.api.RobotTable;
import org.ingrahamrobotics.robottables.api.TableType;
import org.ingrahamrobotics.robottables.api.UpdateAction;
import org.ingrahamrobotics.robottables.api.listeners.TableUpdateListener;

public class InternalTable implements RobotTable {

    private final TablesInterfaceHandler robotTables;
    private final Hashtable valueMap = new Hashtable(); // Map from String to String
    private final List listeners = new ArrayList(); // List of TableUpdateListener
    private TableType type;
    private final String name;
    private long lastUpdate;
    /**
     * Whether this table is confirmed to be owned by us. This is only used internally.
     */
    private boolean readyToPublish;

    public InternalTable(final TablesInterfaceHandler tables, final String name, final TableType initialType) {
        robotTables = tables;
        this.type = initialType;
        this.name = name;
    }

    public TableType getType() {
        return type;
    }

    void setType(TableType type) {
        this.type = type;
    }

    public long getTimeSinceLastUpdate() {
        return System.currentTimeMillis() - lastUpdate;
    }

    public String getName() {
        return name;
    }

    void updatedNow() {
        lastUpdate = System.currentTimeMillis();
    }

    void setReadyToPublish(final boolean readyToPublish) {
        this.readyToPublish = readyToPublish;
    }

    public boolean isReadyToPublish() {
        return readyToPublish;
    }

    public void addUpdateListener(final TableUpdateListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void remoteUpdateListener(final TableUpdateListener listener) {
        listeners.remove(listener);
    }

    public String get(final String key) {
        return (String) valueMap.get(key);
    }

    public String get(final String key, final String defaultValue) {
        String value = (String) valueMap.get(key);
        if (value == null) {
            value = defaultValue;
        }
        return value;
    }

    public int getInt(final String key) {
        String str = (String) valueMap.get(key);
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    public int getInt(final String key, final int defaultValue) {
        String str = (String) valueMap.get(key);
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    public double getDouble(final String key) {
        String str = (String) valueMap.get(key);
        try {
            return Double.parseDouble(str);
        } catch (NumberFormatException ex) {
            return 0.0;
        }
    }

    public double getDouble(final String key, final double defaultValue) {
        String str = (String) valueMap.get(key);
        try {
            return Double.parseDouble(str);
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    public boolean getBoolean(final String key) {
        String str = (String) valueMap.get(key);
        try {
            return Boolean.parseBoolean(str);
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    public boolean getBoolean(final String key, final boolean defaultValue) {
        String str = (String) valueMap.get(key);
        try {
            return Boolean.parseBoolean(str);
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    public long getLong(final String key) {
        String str = (String) valueMap.get(key);
        try {
            return Long.parseLong(str);
        } catch (NumberFormatException ex) {
            return 0l;
        }
    }

    public long getLong(final String key, final long defaultValue) {
        String str = (String) valueMap.get(key);
        try {
            return Long.parseLong(str);
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    public boolean contains(final String key) {
        return valueMap.containsKey(key);
    }

    public boolean isInt(final String key) {
        String str = (String) valueMap.get(key);
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    public boolean isDouble(final String key) {
        String str = (String) valueMap.get(key);
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    public boolean isBoolean(final String key) {
        String str = (String) valueMap.get(key);
        try {
            Boolean.parseBoolean(str);
            return true;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    public String set(final String key, final String value) {
        if (type != TableType.LOCAL) {
            throw new IllegalStateException("TableType is not LOCAL");
        }
        String oldValue = (String) valueMap.get(key);
        if (value == null) {
            if (oldValue != null) {
                // If we do have a value, remove it
                valueMap.remove(key);
                robotTables.internalKeyRemoved(this, key);
            }
        } else {
            if (oldValue == null || !value.equals(oldValue)) {
                // If the value isn't there, or has changed.
                valueMap.put(key, value);
                sendUpdateKeyEvent(key, value, (oldValue == null) ? UpdateAction.NEW : UpdateAction.UPDATE);
                robotTables.internalKeyUpdated(this, key, value);
            }
        }
        return oldValue;
    }

    public void clear() {
        if (type != TableType.LOCAL) {
            throw new IllegalStateException("TableType is not LOCAL");
        }
        if (!valueMap.isEmpty()) {
            valueMap.clear();
            robotTables.internalTableCleared(this);
            sendClearTableEvent();
        }
    }

    /**
     * Sets key to value, for internal use only. Doesn't call internal methods on the TablesInterfaceHandler. A null
     * value will result in the key being removed
     */
    public void internalSet(final String key, final String value) {
        String oldValue = (String) valueMap.get(key);
        if (value == null) {
            if (oldValue != null) {
                valueMap.remove(key);
                sendUpdateKeyEvent(key, null, UpdateAction.DELETE);
            }
        } else {
            if (oldValue == null || !value.equals(oldValue)) {
                valueMap.put(key, value);
                sendUpdateKeyEvent(key, value, (oldValue == null) ? UpdateAction.NEW : UpdateAction.UPDATE);
            }
        }
    }

    /**
     * Clears the table, for internal use only. Doesn't call internal methods on the TablesInterfaceHandler.
     */
    public void internalClear() {
        if (!valueMap.isEmpty()) {
            valueMap.clear();
            sendClearTableEvent();
        }
    }

    /**
     * Gets the internal value set, for internal use only.
     */
    public Hashtable getInternalValues() {
        return valueMap;
    }

    private void sendUpdateKeyEvent(final String key, final String value, final UpdateAction action) {
        for (int i = 0; i < listeners.size(); i++) {
            TableUpdateListener listener = (TableUpdateListener) listeners.get(i);
            listener.onUpdateKey(this, key, value, action);
        }
    }

    private void sendClearTableEvent() {
        for (int i = 0; i < listeners.size(); i++) {
            TableUpdateListener listener = (TableUpdateListener) listeners.get(i);
            listener.onTableCleared(this);
        }
    }
}
