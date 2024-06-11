package org.example.device.config;

import org.example.device.config.mapper.AttributeMapper;

import java.util.ArrayList;
import java.util.List;

public class DeviceConfig {
    private String deviceTypeDes;

    private String deviceType;

    private String deviceDataTypeNumber;

    private String host;

    private int port;

    private String fileCacheDir;

    private AttributeMapper attributeMapper;

    private String icdName;

    private List<UrcbConfig> urcbs = new ArrayList<>();

    private List<BrcbConfig> brcbs = new ArrayList<>();

    private boolean hasFile;

    private String fileRootPath;

    private String deviceName;

    private String device;

    private boolean deviceTime = true;

    private boolean gi = true;

    private boolean report = true;

    private int interval = 600000;

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public String getDeviceType() {
        return this.deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getDeviceDataTypeNumber() {
        return this.deviceDataTypeNumber;
    }

    public void setDeviceDataTypeNumber(String deviceDataTypeNumber) {
        this.deviceDataTypeNumber = deviceDataTypeNumber;
    }

    public String getHost() {
        return this.host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return this.port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getFileCacheDir() {
        return this.fileCacheDir;
    }

    public void setFileCacheDir(String fileCacheDir) {
        this.fileCacheDir = fileCacheDir;
    }

    public AttributeMapper getAttributeMapper() {
        return this.attributeMapper;
    }

    public void setAttributeMapper(AttributeMapper attributeMapper) {
        this.attributeMapper = attributeMapper;
    }

    public String getIcdName() {
        return this.icdName;
    }

    public void setIcdName(String icdName) {
        this.icdName = icdName;
    }

    public List<UrcbConfig> getUrcbs() {
        return this.urcbs;
    }

    public void setUrcbs(List<UrcbConfig> urcbs) {
        this.urcbs = urcbs;
    }

    public List<BrcbConfig> getBrcbs() {
        return this.brcbs;
    }

    public void setBrcbs(List<BrcbConfig> brcbs) {
        this.brcbs = brcbs;
    }

    public boolean isHasFile() {
        return this.hasFile;
    }

    public void setHasFile(boolean hasFile) {
        this.hasFile = hasFile;
    }

    public String getFileRootPath() {
        return this.fileRootPath;
    }

    public void setFileRootPath(String fileRootPath) {
        this.fileRootPath = fileRootPath;
    }

    public String getDeviceTypeDes() {
        return this.deviceTypeDes;
    }

    public void setDeviceTypeDes(String deviceTypeDes) {
        this.deviceTypeDes = deviceTypeDes;
    }

    public void setDeviceTime(boolean flag) {
        this.deviceTime = flag;
    }

    public boolean isDeviceTime() {
        return this.deviceTime;
    }

    public void setGi(boolean flag) {
        this.gi = flag;
    }

    public boolean isGi() {
        return this.gi;
    }

    public boolean isReport() {
        return report;
    }

    public void setReport(boolean report) {
        this.report = report;
    }

    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public String toString() {
        return "DeviceConfig{deviceTypeDes='" + this.deviceTypeDes + '\'' + ", deviceType='"
            + this.deviceType + '\'' + ", deviceDataTypeNumber='" + this.deviceDataTypeNumber + '\''
            + ", host='" + this.host + '\'' + ", port=" + this.port + ", fileCacheDir='" + this.fileCacheDir
            + '\'' + ", attributeMapper=" + this.attributeMapper + ", icdName='" + this.icdName
            + '\'' + ", urcbs=" + this.urcbs + ", brcbs=" + this.brcbs + ", hasFile=" + this.hasFile
            + ", fileRootPath='" + this.fileRootPath + '\'' + '}';
    }
}
