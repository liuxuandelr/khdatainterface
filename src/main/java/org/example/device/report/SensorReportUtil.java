package org.example.device.report;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.example.utils.ReportUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SensorReportUtil {

    public static JSONObject buildJsonObject(String type, String sensorid, String equipmentid,
        String phase, String timestamp, Map<String, Object> itemData) {
        JSONObject jsonObject = new JSONObject(true);
        jsonObject.put("id", ReportUtil.createID());
        jsonObject.put("type", type);
        jsonObject.put("timestamp", timestamp);
        jsonObject.put("sensorid", sensorid);
        jsonObject.put("equipmentid", equipmentid);
        jsonObject.put("Phase", phase);

        List<Map<String, Object>> items = new ArrayList<>();
        for (String itemKey : itemData.keySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("name", itemKey);
            item.put("value", itemData.get(itemKey));
            item.put("desc", itemKey);
            item.put("alarm", "");
            items.add(item);
        }

        jsonObject.put("attrs", items);
        return jsonObject;
    }

//    public static void submitJsonData(String deviceType, JSONObject jsonObject) {
//        JSONArray jsonArrayData = new JSONArray(1);
//        jsonArrayData.add(jsonObject);
//        SendExecutor.submit(new DataSender(jsonArrayData,
//            new DataType(StringUtils.upperCase(deviceType + "_data"))));
//    }

}
