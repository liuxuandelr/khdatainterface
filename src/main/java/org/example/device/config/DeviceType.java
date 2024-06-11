package org.example.device.config;

public class DeviceType {
    private String typeCode;

    private String sheetName;

    private Integer sheetIndex;

    private String typeDataNum;

    private String description;

    public String getTypeCode() {
        return this.typeCode;
    }

    public void setTypeCode(String typeCode) {
        this.typeCode = typeCode;
    }

    public String getSheetName() {
        return this.sheetName;
    }

    public void setSheetName(String sheetName) {
        this.sheetName = sheetName;
    }

    public Integer getSheetIndex() {
        return this.sheetIndex;
    }

    public void setSheetIndex(Integer sheetIndex) {
        this.sheetIndex = sheetIndex;
    }

    public String getTypeDataNum() {
        return this.typeDataNum;
    }

    public void setTypeDataNum(String typeDataNum) {
        this.typeDataNum = typeDataNum;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String toString() {
        return "DeviceType{typeCode='" + this.typeCode + '\'' + ", sheetName='" + this.sheetName + '\'' + ", sheetIndex=" + this.sheetIndex + ", typeDataNum='" + this.typeDataNum + '\'' + ", description='" + this.description + '\'' + '}';
    }
}
