package org.example.config;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Slf4j
public class ServerDataConfig {
    private String type;
    private Integer delay;
    private Integer period;
    private Long intervalLimit;
    private Float multiple;
    private List<String> report;
    private List<SensorItem> sensors;
    private Boolean test;
    private String healthAttr;
    private PointDataMode mode;

    @JSONField(serialize = false, deserialize = false)
    private Map<String, MeasureItem> caIoaMap = new HashMap<>();

    @JSONField(serialize = false, deserialize = false)
    private Map<String, SensorItem> sensorMap = new HashMap<>();

    public void buildDict() {
        if (mode == null) {
            this.mode = PointDataMode.record;
        }

        if (sensors == null) {
            this.sensors = new ArrayList<>();
            return;
        }
        for (SensorItem sensorItem : sensors) {
            sensorItem.buildDict();
            for (MeasureItem measureItem : sensorItem.getAttrs()) {
                if (measureItem.getCa() == 0) {
                    measureItem.setCa(1);
                }
                String measureKey = measureItem.getCa() + "#" + measureItem.getIoa();
                caIoaMap.put(measureKey, measureItem);
                if (StringUtils.isBlank(measureItem.getMeasurementType())) {
                    measureItem.setMeasurementType(measureItem.getIoa() + "#" + measureItem.getMeasurementName());
                }
            }
            sensorMap.put(sensorItem.getSensorId(), sensorItem);
        }
        if (this.delay == null) {
            this.delay = 60000;
        }
        if (this.period == null) {
            this.period = 600;
        }
        if (this.intervalLimit == null) {
            this.intervalLimit = 7200L;
        }
        if (this.multiple == null) {
            this.multiple = 100.0f;
        }
        if (this.test == null) {
            this.test = false;
        }
        /*
        if (this.healthAttr == null) {
            this.healthAttr = "PhyHealth";
        }
         */
        log.info("IEC104-CONFIG: {}", JSONObject.toJSONString(caIoaMap));
    }

    public static void main(String[] args) {
        String configStr = "{\n" +
            "    \"type\": \"021002\",\n" +
            "    \"sensors\": [\n" +
            "        {\n" +
            "            \"name\": \"2号主变A相\",\n" +
            "            \"sensorId\": \"08000205330557000301\",\n" +
            "            \"equipmentId\": \"08000205570001000201\",\n" +
            "            \"phase\": \"A相\",\n" +
            "            \"attrs\": [\n" +
            "                {\n" +
            "                    \"measurementType\": \"C2H2\",\n" +
            "                    \"ca\": 100,\n" +
            "                    \"ioa\": 1,\n" +
            "                    \"typeId\": 12\n" +
            "                },\n" +
            "                {\n" +
            "                    \"measurementType\": \"CO\",\n" +
            "                    \"ca\": 100,\n" +
            "                    \"ioa\": 2,\n" +
            "                    \"typeId\": 12\n" +
            "                },\n" +
            "                {\n" +
            "                    \"measurementType\": \"CO2\",\n" +
            "                    \"ca\": 100,\n" +
            "                    \"ioa\": 3,\n" +
            "                    \"typeId\": 12\n" +
            "                }\n" +
            "            ]\n" +
            "        }\n" +
            "    ]\n" +
            "}";
        ServerDataConfig config = JSONObject.parseObject(configStr, ServerDataConfig.class);
        config.buildDict();
        System.out.println(config.getType());
    }
}
