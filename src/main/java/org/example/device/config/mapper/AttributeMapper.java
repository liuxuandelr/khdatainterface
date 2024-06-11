package org.example.device.config.mapper;

import org.example.device.config.PrefixGroupConfig;

import java.util.*;
import java.util.Map.Entry;

/**
 * @author Admin
 */
public class AttributeMapper {
    /**
     * 监测量：变化即上送
     */
    private Set<String> watchDOIs = new HashSet<>();

    /**
     * 监测装置描述
     */
    private String lnDesc;

    private Map<String, Map<String, String>> ids = new HashMap<>();

    private final Map<String, Map<String, String>> waveFileIds = new HashMap<>();

    /**
     * 监测数据对象描述
     */
    private String lnDoiDesc;

    /**
     * 通道自检情况
     */
    private String lnMoDevFlt;

    /**
     * 数据对象映射
     */
    private Map<String, String> attrRefs = new HashMap<>();

    private Map<String, PrefixGroupConfig.PrefixGroupItem> prefixGroupItemMap = new HashMap<>();

    public Set<String> getWatchDOIs() {
        return this.watchDOIs;
    }

    public String getLnDesc() {
        return lnDesc;
    }

    public void setLnDesc(String lnDesc) {
        this.lnDesc = lnDesc;
    }

    public Map<String, String> getAttrRefs() {
        return attrRefs;
    }

    public void setAttrRefs(Map<String, String> attrRefs) {
        this.attrRefs = attrRefs;
    }

    public Map<String, Map<String, String>> getIds() {
        return ids;
    }

    public void setIds(Map<String, Map<String, String>> ids) {
        this.ids = ids;
    }

    public Map<String, Map<String, String>> getWavFileIds() {
        return waveFileIds;
    }

    public String getLnDoiDesc() {
        return lnDoiDesc;
    }

    public void setLnDoiDesc(String lnDoiDesc) {
        this.lnDoiDesc = lnDoiDesc;
    }

    public String getLnMoDevFlt() {
        return lnMoDevFlt;
    }

    public void setLnMoDevFlt(String lnMoDevFlt) {
        this.lnMoDevFlt = lnMoDevFlt;
    }

    public String getKeyByValue(String value) {
        Iterator<Entry<String, String>> iter = attrRefs.entrySet().iterator();
        while (iter.hasNext()) {
            Entry<String, String> entry = iter.next();
            if (entry.getValue().equals(value))
                return entry.getKey();
        }
        return null;
    }

    public String getValueByKey(String value) {
        return attrRefs.get(value);
    }

    public Map<String, PrefixGroupConfig.PrefixGroupItem> getPrefixGroupItemMap() {
        return this.prefixGroupItemMap;
    }

    @Override
    public String toString() {
        return "AttributeMapper [lnDesc=" + lnDesc + ", lnDoiDesc=" + lnDoiDesc + ", attrRefs=" + attrRefs + "]";
    }

}
