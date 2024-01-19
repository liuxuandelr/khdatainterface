package org.example.config;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

@Data
public class MeasureItem {
    private String measurementType;
    private String measurementName;
    private int ca = 1;
    private int ioa;
    private Integer typeId;

    @JSONField(serialize = false, deserialize = false)
    private SensorItem sensorItem;


}
