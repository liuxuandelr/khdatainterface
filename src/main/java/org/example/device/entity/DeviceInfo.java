package org.example.device.entity;

import lombok.Getter;
import lombok.Setter;
import org.example.device.Client;
import org.example.device.report.DataReportAcceptor;
import org.example.monitor.CacIedStatus;
import org.example.monitor.SensorStatus;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;


@Getter
@Setter
public class DeviceInfo {

    private String deviceName;
    private String deviceType;
    private Integer status;
    private DEVICE_STATUS deviceStatus = DEVICE_STATUS.normal;
    private DataReportAcceptor dataReportAcceptor;
    private Client client;
    private Boolean initStatus = false;


    // 上次更新时间
    private Long lastUpdateTime;
    // 上次数据的采集时间
    private Long lastGatherTime;
    private String lastGatherTimeStr;
    private String lastUpdateTimeStr;

    private HashMap<String, SensorStatus> sensorsMap;
    private HashMap<String, Integer> sensorsCounts = new HashMap<>();
    private HashSet<String> sensorSet = new HashSet<>();
    private CacIedStatus cacIedStatus;

    // TODO 同时放入最新的数据属性 和属性值
    private HashMap<String, String> attrValues;


    public DeviceInfo(Client client) {
        this.client = client;
        this.deviceName = client.getDevName();
        this.deviceType = client.getDevType();
        this.dataReportAcceptor = client.getDataAcceptor();
        this.cacIedStatus = new CacIedStatus();
        this.cacIedStatus.setDevName(client.getDevName());
        this.cacIedStatus.setIp(client.getDeviceConfig().getHost());
    }

    public void againInitDeviceInfo() {
        this.dataReportAcceptor = client.getDataAcceptor();
        initSensorStatus(true);
    }

    public void setInitStatus(Boolean initStatus) {
        this.initStatus = initStatus;
    }

    public void initSensorStatus(Boolean isInit) {
        if (!isInit && sensorsMap!=null && sensorsMap.size()!=0) {
            for (Map.Entry<String, SensorStatus> stringSensorStatusEntry : sensorsMap.entrySet()) {
                SensorStatus value = stringSensorStatusEntry.getValue();
                value.init();
            }
            return;
        }

        sensorsMap = new HashMap<>();
        List<LNDevice> lnDevices = this.getDataReportAcceptor().getLnDevices();

        for (LNDevice lnDevice : lnDevices) {
            String sensorid = lnDevice.getSensorid();
            SensorStatus sensorStatus = new SensorStatus();
            sensorStatus.setId(sensorid);
            sensorsMap.put(sensorid, sensorStatus);
        }
    }

    public DataReportAcceptor getDataReportAcceptor() {
        if (this.dataReportAcceptor == null) {
            this.dataReportAcceptor = this.client.getDataAcceptor();
            this.dataReportAcceptor = this.client.getDataAcceptor();
        }
        return dataReportAcceptor;
    }
}
