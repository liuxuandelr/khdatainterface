package org.example.device.entity;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSONObject;
import com.beanit.iec61850bean.BasicDataAttribute;
import com.beanit.iec61850bean.BdaQuality;
import com.beanit.iec61850bean.BdaType;
import com.beanit.iec61850bean.FcModelNode;
import org.apache.commons.lang3.StringUtils;
import org.example.config.Config;
import org.example.device.config.PrefixGroupConfig;
import org.example.utils.ReportUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;

public class LNDevice {
    public static Pattern DIGIT_PAT = Pattern.compile("[0-9]+");
    public static Map<String, String> NAME_MAP = new HashMap<>();
    private static final Logger logger = LoggerFactory.getLogger(LNDevice.class);
    private String ref;
    private String type;
    private String desc;
    private String dateTime;
    private volatile long lastUpdateTime = 0;
    private List<DOItem> doItems;
    private boolean fileChanged = false;
    private String waveFileSTime;
    private final int i = 0;
    private DOItem moDevFlt;
    private String sensorid;
    private String equipmentid;
    private String phase;
    private boolean isSetDate = false;
    private int watchCount = 0;
    private Map<String, String> watchValues = new TreeMap<>();

    private Map<String, PrefixGroupConfig.PrefixGroupItem> prefixGroupItemMap;

    static {
        NAME_MAP.put("EEHealth".toUpperCase(), "Health");
    }

    public String getPreIsSetDate(DOItem doItem) {
        if (isSetDate && doItem != null) {
            DateTime doItemDate = getDOItemDate(doItem);
            if (doItemDate != null) {
                return "$$" + doItemDate.toString();
            } else {
                return "";
            }
        }
        return "";

    }

    public boolean getIsSetDate() {
        return isSetDate;
    }

    public void setIsSetDate(boolean isSetDate) {
        this.isSetDate = isSetDate;
    }

    public String getSensorid() {
        return this.sensorid;
    }

    public void setSensorid(String sensorid) {
        this.sensorid = sensorid;
    }

    public String getEquipmentid() {
        return this.equipmentid;
    }

    public void setEquipmentid(String equipmentid) {
        this.equipmentid = equipmentid;
    }

    public List<DOItem> getDoItems() {
        return this.doItems;
    }

    public void setDoItems(List<DOItem> doItems) {
        this.doItems = doItems;
    }

    public String getRef() {
        return this.ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDesc() {
        return this.desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getDateTime() {
        return this.dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

//    public void updateDateTime(String dateTime) {
////        DateTime curDateTime = DateUtil.parse(dateTime, ConfigConstant.SIMPLE_DATE_FORMAT);
//        this.dateTime = dateTime;
//
//        /**
//         * 时间戳作为字段上送时，解析为记录时间
//         */
//        for (DOItem doItem : this.doItems) {
//            if (doItem.getDestAttr().equalsIgnoreCase("TestTime")) {
//                List<BasicDataAttribute> dataAttrs = doItem.getDataAttributes();
//                String timeStr = dataAttrs.get(0).getValueString();
//                DateTime testDate;
//                if (DIGIT_PAT.matcher(timeStr).matches()) {
//                    testDate = new DateTime(Long.valueOf(timeStr));
//                } else {
//                    testDate = curDateTime;
//                }
//                if (testDate != null) {
//                    this.dateTime = testDate.toString();
//                    return;
//                }
//                break;
//            }
//        }
//
//        DateTime maxDoItemDate = null;
//        DOItem maxDoItem = null;
//        for (DOItem doItem : this.doItems) {
//            DateTime doItemDate = getDOItemDate(doItem);
//            if (doItemDate == null) {
//                continue;
//            }
//            if (StringUtils.equalsIgnoreCase("MoDevConf", doItem.getDestAttr())) {
//                if (doItem.getDataAttributes().size() > 0) {
//                    String valStr = doItem.getDataAttributes().get(0).getValueString();
//                    if (StringUtils.equalsIgnoreCase("false", valStr) || StringUtils.equals("0", valStr)) {
//                        continue;
//                    }
//                }
//            }
//            if (maxDoItemDate == null || doItemDate.getTime() > maxDoItemDate.getTime()) {
//                maxDoItemDate = doItemDate;
//                maxDoItem = doItem;
//            }
//        }
//        try {
//            long maxItemTime = 0;
//            if (maxDoItemDate != null) {
//                maxItemTime = maxDoItemDate.getTime();
//                LogUtil.test(logger, maxDoItem.getDesc() + "----------maxDoItemDate: "
//                    + maxDoItemDate);
//            }
//
//            long between = curDateTime.getTime() - maxItemTime;
//            if (Config.getSetLocalDate()
//                && (between >= Config.getSetLocalDateLaterError()
//                || between <= -Config.getSetLocalDateError())) {
//                this.isSetDate = true;
//                return;
//            }
//            this.dateTime = maxDoItemDate.toString();
//        } catch (Exception e) {
//            logger.error("set date error: " + e);
//        }
//    }

    public DateTime getDOItemDate(DOItem doItem) {
        List<BasicDataAttribute> dataAttrs = doItem.getDataAttributes();
        DateTime maxDate = null;
        if (dataAttrs != null && dataAttrs.size() > 0) {
            for (BasicDataAttribute dataAttr : dataAttrs) {
                if (dataAttr.getBasicType() == BdaType.TIMESTAMP) {
                    try {
                        Instant instant = Instant.from(
                            DateTimeFormatter.ISO_INSTANT.parse(dataAttr.getValueString()));
                        maxDate = new DateTime(instant);
                        //maxDate = DateUtil.parse(dataAttr.getValueString(), ConfigConstant.DATE_FORMAT, Locale.US);
                    } catch (Exception e) {
                        logger.error("parse date error: ", e);
                    }
                    break;
                }
            }
        }
        doItem.setMaxAttrDate(maxDate);
        return maxDate;
    }

    public long getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(long lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public boolean isFileChanged() {
        return this.fileChanged;
    }

    public void setFileChanged(boolean fileChanged) {
        this.fileChanged = fileChanged;
    }

    public String getWaveFileSTime() {
        return this.waveFileSTime;
    }

    public void setWaveFileSTime(String waveFileSTime) {
        this.waveFileSTime = waveFileSTime;
    }

    public DOItem getMoDevFlt() {
        return this.moDevFlt;
    }

    public void setMoDevFlt(DOItem moDevFlt) {
        this.moDevFlt = moDevFlt;
    }

    public String getPhase() {
        return this.phase;
    }

    public void setPhase(String phase) {
        this.phase = phase;
    }

    public List<DOItem> updateFcNodes(List<FcModelNode> fcNodes) {
        List<DOItem> result = new ArrayList<>();
        synchronized (this) {
            for (FcModelNode fcNode : fcNodes) {
                String ref = Objects.toString(fcNode.getReference(), "");
                List<BasicDataAttribute> attributes = fcNode.getBasicDataAttributes();
                if (this.moDevFlt != null && this.moDevFlt.getRef().equals(ref)) {
                    this.moDevFlt.setDataAttributes(attributes);
                    if (attributes.size() > 0) {
                        moDevFlt.setValue(attributes.get(0).getValueString());
                    }
                    continue;
                }
                for (DOItem doItem : this.getDoItems()) {
                    if (doItem.getRef().equals(ref)) {
                        doItem.setDataAttributes(attributes);
                        if (attributes.size() > 0) {
                            String value = attributes.get(0).getValueString();
                            if (doItem.isWatched() && !StringUtils.equals(value, doItem.getValue())) {
                                result.add(doItem);
                            }
                            doItem.setValue(value);
                        }
                        break;
                    }
                }
            }
        }
        return result;
    }

    @Override
    public String toString() {
        return "LNDevice{" +
            "ref='" + ref + '\'' +
            ", type='" + type + '\'' +
            ", desc='" + desc + '\'' +
            ", dateTime='" + dateTime + '\'' +
            ", doItems=" + doItems +
            ", fileChanged=" + fileChanged +
            ", waveFileSTime='" + waveFileSTime + '\'' +
            ", i=" + i +
            ", moDevFlt=" + moDevFlt +
            ", sensorid='" + sensorid + '\'' +
            ", equipmentid='" + equipmentid + '\'' +
            ", phase='" + phase + '\'' +
            '}';
    }

    public List<JSONObject> getJsonObjects() {
        List<JSONObject> result = new ArrayList<>();
        if (this.prefixGroupItemMap == null || this.prefixGroupItemMap.size() == 0) {
            JSONObject jsonObject =
                buildJsonObject(this.getSensorid(), this.getEquipmentid(), this.getDoItems(), null);
            if (jsonObject != null) {
                result.add(jsonObject);
            }
            return result;
        }

        for (String key : this.prefixGroupItemMap.keySet()) {
            List<DOItem> subGroup = new ArrayList<>();
            for (DOItem doItem : getDoItems()) {
                if (doItem.getDestAttr().startsWith(key)) {
                    subGroup.add(doItem);
                }
            }
            if (subGroup.isEmpty()) {
                continue;
            }
            PrefixGroupConfig.PrefixGroupItem groupItem = this.prefixGroupItemMap.get(key);
            JSONObject jsonObject = this.buildJsonObject(groupItem.getSensorId(),
                groupItem.getEquipmentId(), subGroup, key);
            if (jsonObject != null) {
                result.add(jsonObject);
            }
        }
        return result;
    }

    public JSONObject buildJsonObject(String sensorid, String equipmentid, List<DOItem> doItems, String prefix) {
        JSONObject jsonObject = new JSONObject(true);
        jsonObject.put("type", getType());
        jsonObject.put("timestamp", getDateTime());
        jsonObject.put("sensorid", sensorid);
        jsonObject.put("equipmentid", equipmentid);

        boolean prefixTrim = StringUtils.isNoneBlank(prefix);
        List<Map<String, Object>> items = new ArrayList<>();
        int validCount = 0;
        for (DOItem doItem : doItems) {
            Map<String, Object> item = new HashMap<>();
            String name = null;
            if (prefixTrim) {
                name = doItem.getDestAttr().replaceAll("^" + prefix, "");
            } else {
                name = doItem.getDestAttr();
            }

            if (StringUtils.isBlank(name)) {
                continue;
            }

            if (name != null) {
                name = NAME_MAP.getOrDefault(name.toUpperCase(), name);
                item.put("name", name);
            }
            item.put("desc", doItem.getDesc() + getPreIsSetDate(doItem));

            String quality = "0";
            List<BasicDataAttribute> dataAttrs = doItem.getDataAttributes();
            if (dataAttrs != null && dataAttrs.size() > 0) {
                item.put("value", StringUtils.trim(dataAttrs.get(0).getValueString()));
                if (!StringUtils.equalsIgnoreCase("Health", name)) {
                    ++validCount;
                }
                try {
                    for (BasicDataAttribute attr : dataAttrs) {
                        if (attr instanceof BdaQuality) {
                            BdaQuality qualityAttr = (BdaQuality) attr;
                            quality = "" + qualityAttr.getValidity().getIntValue();
                        }
                    }
                } catch (Exception e) {
//                    logger.error("QUALITY wrong: {}, {}, {}, {} - {}", getDateTime(), getType(), sensorid, name, e);
                }
            }
            item.put("quality", quality);
            item.put("alarm", "");
            items.add(item);
        }
        if (validCount == 0) {
            return null;
        }
        if (getPhase() != null && getPhase().length() != 0) {
            Map<String, Object> item = new HashMap<>();
            item.put("name", "Phase");
            item.put("value", getPhase());
            item.put("desc", "相别");
            item.put("quality", "0");
            item.put("alarm", "");
            items.add(item);
        }
        jsonObject.put("attrs", items);
        return jsonObject;
    }

    public JSONObject getJsonObject() {
        return buildJsonObject(this.getSensorid(), this.getEquipmentid(), this.getDoItems(), null);
    }

    public JSONObject getHeartBeatInfo() {
        JSONObject jsonObject = new JSONObject(true);
        jsonObject.put("status", getStatus());
        jsonObject.put("operationtemperature",
            Integer.valueOf((new Random(System.currentTimeMillis())).nextInt(20)));
        return jsonObject;
    }

    private String getStatus() {
        String status = "NORMAL";
        return status;
    }

    public void restTimeTask(FileReporterInfo fileInfo) {
        cancelTimeTask();
    }

    public void cancelTimeTask() {
    }

    public Map<String, PrefixGroupConfig.PrefixGroupItem> getPrefixGroupItemMap() {
        return prefixGroupItemMap;
    }

    public void setPrefixGroupItemMap(
        Map<String, PrefixGroupConfig.PrefixGroupItem> prefixGroupItemMap) {
        this.prefixGroupItemMap = prefixGroupItemMap;
    }

    public void setWatchCount(int watchCount) {
        this.watchCount = watchCount;
    }

    public int getWatchCount() {
        return watchCount;
    }

    public static String attrsMd5(List<Map<String, Object>> items) {
        TreeMap<String, String> sortData = new TreeMap<>();
        for (Map<String, Object> item : items) {
            sortData.put((String) item.get("name"), (String) item.get("value"));
        }
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, String> keyVal : sortData.entrySet()) {
            builder.append(keyVal.getKey()).append("=")
                .append(keyVal.getValue() == null ? "" : keyVal.getValue()).append(";");
        }
        return ReportUtil.encrypByMD5(builder.toString());
    }

    public synchronized boolean updateWatchValues() {
        boolean updated = false;
        for (DOItem doItem : this.doItems) {
            if (doItem.getValue() == null || !doItem.isWatched()) {
                continue;
            }
            String prevValue = this.watchValues.get(doItem.getSrcAttr());
            if (!StringUtils.equals(prevValue, doItem.getValue())) {
                this.watchValues.put(doItem.getSrcAttr(), doItem.getValue());
                updated = true;
            }
        }
        return updated;
    }
}
