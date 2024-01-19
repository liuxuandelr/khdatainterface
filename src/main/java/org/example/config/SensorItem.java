package org.example.config;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Data
public class SensorItem {
    private String name;
    private String sensorId;
    private String equipmentId;
    private String phase;
    private List<MeasureItem> attrs;

    @JSONField(serialize = false, deserialize = false)
    private long lastModified;

    public void buildDict() {
        if (attrs == null) {
            attrs = new ArrayList<>();
            return;
        }
        for (MeasureItem item : attrs) {
            item.setSensorItem(this);
        }
        if (StringUtils.isBlank(equipmentId)) {
            this.equipmentId = sensorId;
        }
    }
}
