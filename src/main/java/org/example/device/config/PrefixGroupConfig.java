package org.example.device.config;

import com.alibaba.fastjson.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class PrefixGroupConfig {

    public static Map<String, PrefixGroupItem> loadGroupConfig(String fileName) {
        Map<String, PrefixGroupItem> result = new HashMap<>();
        try {
            String configFilePath = System.getProperty("user.dir") + File.separator + "conf"
                + File.separator + fileName;
            String configContent = new String(Files.readAllBytes(Paths.get(configFilePath)), "utf-8");
            JSONObject jsonObject = JSONObject.parseObject(configContent);
            for (String key : jsonObject.keySet()) {
                PrefixGroupItem item = new PrefixGroupItem();
                JSONObject itemObj = jsonObject.getJSONObject(key);
                item.setSensorId(itemObj.getString("sensor_id"));
                item.setEquipmentId(itemObj.getString("equipment_id"));
                result.put(key, item);
            }
        } catch (Exception e) {
            log.error("parse error: ", e);
        }
        return result;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PrefixGroupItem {
        private String sensorId;
        private String equipmentId;
    }

    public static void main(String[] args) {
        Map<String, PrefixGroupItem> configs = PrefixGroupConfig.loadGroupConfig("pdm/hxsh_sensors_1.json");
        System.out.println(configs.keySet());
    }
}
