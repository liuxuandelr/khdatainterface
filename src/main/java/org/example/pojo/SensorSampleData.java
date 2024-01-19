package org.example.pojo;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import org.example.config.AttrData;

import java.text.SimpleDateFormat;
import java.util.*;

@Data
public class SensorSampleData {
    private String type;
    private String name;
    private String sensorid;
    private String equipmentid;
    private String id;
    private String timestamp;
    private List<FileData> files;     //{name: "", value: ""}
    private String fileDesc;
    private List<AttrData> attrs;

    @JSONField(serialize = false, deserialize = false)
    private Map<String, AttrData> currentValues;

    @JSONField(serialize = false, deserialize = false)
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @JSONField(serialize = false, deserialize = false)
    private long lastModified;

    @JSONField(serialize = false, deserialize = false)
    private long lastAlarmed;

    private long lastReadTime = -1;

    public SensorSampleData() {
        this.attrs = new ArrayList();
        this.files = new ArrayList();
        this.currentValues = new Hashtable<>();
        this.lastModified = new Date().getTime();
        this.lastAlarmed = -1;
    }

    public SensorSampleData(String type, String sensorid, String equipmentid, String timestamp) {
        this.id = UUID.randomUUID().toString().replaceAll("-", "");
        this.type = type;
        this.sensorid = sensorid;
        this.equipmentid = equipmentid;
        this.timestamp = timestamp;
        this.files = new ArrayList();
        this.attrs = new ArrayList();
        this.currentValues = new Hashtable<>();
        this.lastModified = new Date().getTime();
        this.lastAlarmed = -1;
    }

//    public synchronized boolean addAttr(AttrData attrData) {
//        AttrData prevAttr = currentValues.get(attrData.getName());
//        if (prevAttr == null || prevAttr.getFlWavePeerT().compareTo(attrData.getFlWavePeerT())!=0) {
//            this.lastModified = attrData.getLastUpdate();
//            currentValues.put(attrData.getName(), attrData);
//        } else {
//            attrData.setLastUpdate(this.lastModified);
//        }
//        return true;
//    }

//    public SensorSampleData readData(String timestamp, long readTime, ServerDataConfig dataConfig) {
//        long limit = (dataConfig.getPeriod() + 60) * 1000;
//        if (readTime - this.lastReadTime < limit || this.lastModified < this.lastReadTime) {
//            return null;
//        }
//        this.lastReadTime = readTime;
//        SensorSampleData sampleData = new SensorSampleData(type, sensorid, equipmentid, timestamp);
//        AttrData c2h2 = null;
//        AttrData c2h6 = null;
//        AttrData ch4 = null;
//        AttrData c2h4 = null;
//        AttrData totHyd = null;
//
//        for (AttrData attrData : this.currentValues.values()) {
//            AttrData copyAttrData = new AttrData(attrData);
//            String tag = attrData.getName();
//            if (StringUtils.isBlank(tag)) {
//                continue;
//            }
//            if (copyAttrData.getLastUpdate() > this.lastReadTime) {
//                this.lastReadTime = copyAttrData.getLastUpdate();
//            }
//            sampleData.getAttrs().add(copyAttrData);
//            if (tag.equalsIgnoreCase("C2H2")) {
//                c2h2 = copyAttrData;
//            }
//            if (tag.equalsIgnoreCase("C2H6")) {
//                c2h6 = copyAttrData;
//            }
//            if (tag.equalsIgnoreCase("CH4")) {
//                ch4 = copyAttrData;
//            }
//            if (tag.equalsIgnoreCase("C2H4")) {
//                c2h4 = copyAttrData;
//            }
//            if (tag.equalsIgnoreCase("TotHyd")) {
//                totHyd = copyAttrData;
//            }
//        }
//
//        if (c2h2 != null && c2h6 != null && ch4 != null && c2h4 != null) {
//            if (totHyd == null) {
//                totHyd = new AttrData();
//                totHyd.setName("TotHyd");
//                totHyd.setDesc("TotHyd");
//                sampleData.getAttrs().add(totHyd);
//            }
//            try {
//                Float c2h2Val = Float.valueOf(c2h2.getValue());
//                Float c2h6Val = Float.valueOf(c2h6.getValue());
//                Float ch4Val = Float.valueOf(ch4.getValue());
//                Float c2h4Val = Float.valueOf(c2h4.getValue());
//
//                if (c2h2Val >= 0 && c2h6Val >= 0 && ch4Val >= 0 && c2h4Val >= 0) {
//                    totHyd.setValue(String.valueOf(c2h2Val + c2h6Val + ch4Val + c2h4Val));
//                }
//            } catch (Exception e) {
//            }
//        }
//
//        return sampleData;
//    }

    public boolean checkSensorError(long nowMil, long limit) {
        return (nowMil + 120000 - this.lastModified >= limit * 1000);
    }
}
