package org.example.pojo;

import com.alibaba.fastjson.annotation.JSONType;
import lombok.Data;

@Data
@JSONType(orders = {"sensorId","substationsId","deviceIds","linesId","deviceCode","linesCode","nameDesc"})
public class SensorEntity {

    public String sensorId;

    public String substationsId;

    public String deviceIds;

    public String linesId;

    public String deviceCode;

    public String linesCode;

    public String nameDesc;
}
