package org.example.monitor;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import org.example.monitor.status.SensorStatusEnum;

@Data
public class SensorStatus {
    private String id;

    //   MoDevComF
    private String status = SensorStatusEnum.Init.name();
    private String operationtemperature;
    private String lastUpdated;

    // 是否经过退火处理
    @JSONField(serialize = false)
    private Boolean anneal = false;

    @JSONField(serialize = false)
    private int restartCountByError = 0;

    @JSONField(serialize = false)
    private int checkCount = 4;

    @JSONField(serialize = false)
    private boolean alarmed = false;

    @JSONField(serialize = false)
    private long lastUpdatedMils = 0;

    public Boolean checkCount() {
        if (!anneal && restartCountByError == 0) {
            restartCountByError++;
            anneal = true;
            return false;
        }
        if (anneal && restartCountByError>=checkCount) {
            restartCountByError = 0;
            anneal = false;
            checkCount++;
            return false;
        }
        restartCountByError++;
        // 不做处理
        return true;
    }

    public void init() {
        status = SensorStatusEnum.Init.name();
        lastUpdated = null;
    }


    public void setStatus(String status) {
        if (status.equals("false") || status.equals("0")) {
            this.status = SensorStatusEnum.Success.name();
        } else {
            this.status = SensorStatusEnum.Error.name();
        }
    }


    public void setLastUpdated(String lastUpdated) {
        this.lastUpdated = lastUpdated;
        this.status = SensorStatusEnum.Success.name();
    }


    @Override
    public String toString() {
        return "SensorStatus{" +
                "id='" + id + '\'' +
                ", status='" + status + '\'' +
                ", operationtemperature='" + operationtemperature + '\'' +
                ", lastUpdated='" + lastUpdated + '\'' +
                '}';
    }
}
