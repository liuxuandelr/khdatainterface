package org.example.config;

public class DataType {
    public static final DataType ERROR_INFO = new DataType("ERROR_INFO");
    public static final DataType HEARTBEAT = new DataType("HEARTBEAT");
    public static final DataType CALL_DATA = new DataType("CALL_DATA");
    public static final DataType DATA = new DataType("DATA");

    public static final DataType POINT_DATA = new DataType("POINT_DATA");
    String name;

    public DataType(String name) {
        this.name = name.toUpperCase();
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

