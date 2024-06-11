package org.example.device.entity;

import com.beanit.iec61850bean.Brcb;
import com.beanit.iec61850bean.Urcb;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
public class DeviceRcb {
    Map<String, Map<String, List<Brcb>>> freeBrcbMapList;
    Map<String, Map<String, List<Urcb>>> freeUrcbMapList;
    Map<String, Map<String, List<Brcb>>> allBrcbMapList;
    Map<String, Map<String, List<Urcb>>> allUrcbMapList;
    List<String> allUrcbNameList;
    List<String> allBrcbNameList;
    private String deviceRef;
    private Map<String, Integer> enableSeedMap;

    public void setBrcbMapList(Map<String, Map<String, List<Brcb>>> allBrcbMapList,
        Map<String, Map<String, List<Brcb>>> freeBrcbMapList) {
        this.freeBrcbMapList = freeBrcbMapList;
        this.allBrcbMapList = allBrcbMapList;

    }

    public void setUrcbMapList(Map<String, Map<String, List<Urcb>>> allUrcbMapList,
        Map<String, Map<String, List<Urcb>>> freeUrcbMapList) {
        this.freeUrcbMapList = freeUrcbMapList;
        this.allUrcbMapList = allUrcbMapList;
    }

    public void arrangeList() {
        allBrcbNameList = new ArrayList<>();
        allUrcbNameList = new ArrayList<>();
        Set<Map.Entry<String, Map<String, List<Brcb>>>> allBrcbMapListEntries = allBrcbMapList.entrySet();
        for (Map.Entry<String, Map<String, List<Brcb>>> entry : allBrcbMapListEntries) {
            Map<String, List<Brcb>> value = entry.getValue();
            Set<Map.Entry<String, List<Brcb>>> entries1 = value.entrySet();
            for (Map.Entry<String, List<Brcb>> stringListEntry : entries1) {
                allBrcbNameList.add(stringListEntry.getKey());
            }
        }
        Set<Map.Entry<String, Map<String, List<Urcb>>>> entries = allUrcbMapList.entrySet();
        for (Map.Entry<String, Map<String, List<Urcb>>> entry : entries) {
            Map<String, List<Urcb>> value = entry.getValue();
            Set<Map.Entry<String, List<Urcb>>> entries1 = value.entrySet();
            for (Map.Entry<String, List<Urcb>> stringListEntry : entries1) {
                allUrcbNameList.add(stringListEntry.getKey());
            }
        }
    }

    public int getRefEnableSeed(String rcbRef) {
        return enableSeedMap.getOrDefault(rcbRef, Integer.valueOf(-1));
    }

    public void setRefEnableSeed(String rcbRef, int seed) {
        enableSeedMap.put(rcbRef, Integer.valueOf(seed));
    }
}
