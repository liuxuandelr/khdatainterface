package org.example.device.report;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.example.device.config.DeviceConfig;
import org.example.device.entity.FileReporterInfo;
import org.example.utils.ReportUtil;

import java.util.*;

@Slf4j
public class DataPacker {

    public static JSONArray pack(DeviceConfig deviceConfig, List<JSONObject> lnDevices,
                                 FileReporterInfo fileInfo, List<Map<String, String>> waveFiles) {
        try {
            Map<String, Map<String, String>> waveFileIds =
                deviceConfig.getAttributeMapper().getWavFileIds();
            Map<String, List<Map<String, String>>> sensorFilesMap = new HashMap<>();
            Map<String, Map<String, String>> sensorExtMap = new HashMap<>();
            for (Map<String, String> file : waveFiles) {
                String fileName = file.get("name");
                Map<String, String> extDef = null;
                for (String waveKey : waveFileIds.keySet()) {
                    if (fileName.contains(waveKey)) {
                        extDef = waveFileIds.get(waveKey);
                        break;
                    }
                }
                if (extDef == null) {
                    log.error("WAVE-NOT-MATCH: {}", fileName);
                    continue;
                }
                String sensorid = extDef.get("ext:uri");
                List<Map<String, String>> sensorFile = sensorFilesMap.get(sensorid);
                if (sensorFile == null) {
                    sensorFile = new ArrayList<>();
                    sensorFilesMap.put(sensorid, sensorFile);
                }
                sensorFile.add(file);
                sensorExtMap.put(sensorid, extDef);
            }
            Date now = new Date();
            fileInfo.setSendDate(now);
            if (waveFiles.size() > 0) {
                log.info("WAVE-CHANGED: {}, {}, {}", deviceConfig.getDeviceTypeDes(), waveFiles.size(),
                    sensorFilesMap.size());
            }
            fileInfo.setSended();
            List<JSONObject> jsonArray = new ArrayList<>();
            int size = lnDevices.size();
            for (int i = 0; i < size; i++) {
                JSONObject jsonObject = lnDevices.get(i);
                String id = ReportUtil.createID();
                jsonObject.put("id", id);
                String sensorid = jsonObject.getString("sensorid");
                List<Map<String, String>> sensorFiles = sensorFilesMap.remove(sensorid);
                if (sensorFiles == null) {
                    sensorFiles = new ArrayList<>();
                }
                jsonObject.put("files", sensorFiles);
                jsonArray.add(jsonObject);
            }

            for (Map.Entry<String, List<Map<String, String>>> fileItem : sensorFilesMap.entrySet()) {
                if (fileItem.getValue() == null || fileItem.getValue().isEmpty()) {
                    log.warn("WAVE-EMPTY: {}", fileItem.getKey());
                    continue;
                }
                String sensorid = fileItem.getKey();
                Map<String, String> extDef = sensorExtMap.get(sensorid);
                for (Map<String, String> fileMap : fileItem.getValue()) {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("id", ReportUtil.createID());
                    jsonObject.put("type", deviceConfig.getDeviceDataTypeNumber());
                    jsonObject.put("sensorid", sensorid);
                    jsonObject.put("equipmentid", extDef.get("ext:devid"));
                    String phase = extDef.get("ext:Phase");
                    jsonObject.put("Phase", phase);
                    if (StringUtils.isNotBlank(phase)) {
                        List<Map<String, Object>> items = new ArrayList<>();
                        Map<String, Object> item = new HashMap<>();
                        item.put("name", "Phase");
                        item.put("value", phase);
                        item.put("desc", "相别");
                        item.put("alarm", "");
                        items.add(item);
                        jsonObject.put("attrs", items);
                    }
                    jsonObject.put("timestamp", fileMap.remove("timestamp"));
                    List<Map<String, String>> upFiles = new ArrayList<>();
                    upFiles.add(fileMap);
                    jsonObject.put("files", upFiles);
                    jsonArray.add(jsonObject);
                }
            }

            JSONArray result = new JSONArray(jsonArray.size());
            result.addAll(jsonArray);
            return result;
        } catch (Exception ex) {
            log.error("[DATA-PACK-ERROR]: 数据打包异常, ", ex);
        }
        return new JSONArray();
    }
}
