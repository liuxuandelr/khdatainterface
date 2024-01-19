package org.example.config;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

@Data
public class AttrData {
    private String name;
    private String alarm;
    private String value;
    private String desc;
    private String quality;

    @JSONField(serialize = false, deserialize = false)
    private long lastUpdate;

    public AttrData() {
    }

    public AttrData(AttrData data) {
        this.name = data.getName();
        this.alarm = data.getAlarm();
        this.value = data.getValue();
        this.desc = data.getDesc();
        this.quality = data.getQuality();
        this.lastUpdate = data.getLastUpdate();
    }

}
