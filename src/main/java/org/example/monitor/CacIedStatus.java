package org.example.monitor;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.example.device.entity.DEVICE_STATUS;
import org.example.monitor.status.IEDStatusEnum;

import java.util.HashMap;

@Data
public class CacIedStatus {
    private String ip;
    private String status = IEDStatusEnum.Init.name();
    private String lastUpdated;
    private String devName;
    private HashMap<String,Integer> counts = new HashMap<>();

    public void setStatus(String status) {
        if (status.equals("false") || status.equals("0")) {
            this.status = IEDStatusEnum.ChannelSuccess.name();
        } else {
            this.status = IEDStatusEnum.ChannelFail.name();
        }
    }

    public void setStatus(DEVICE_STATUS deviceStatus) {
        if (deviceStatus == DEVICE_STATUS.device_network_fail || deviceStatus == DEVICE_STATUS.device_network_fail2
                || deviceStatus == DEVICE_STATUS.not_update1 || deviceStatus == DEVICE_STATUS.not_update2
                || deviceStatus == DEVICE_STATUS.conn_fail || deviceStatus == DEVICE_STATUS.restart_conn_fail
                || deviceStatus == DEVICE_STATUS.device_Acceptor_init_fail || deviceStatus == DEVICE_STATUS.client_init_start_fail
                || deviceStatus == DEVICE_STATUS.restart_conning || deviceStatus == DEVICE_STATUS.restart_conn) {
            this.status = IEDStatusEnum.ConnectError.name();
        } else if (deviceStatus == DEVICE_STATUS.conn_recover || deviceStatus == DEVICE_STATUS.device_network_normal || deviceStatus == DEVICE_STATUS.normal) {
            this.status = IEDStatusEnum.Connected.name();
        }
    }

    public void setLastUpdated(String lastUpdated) {
        this.status = IEDStatusEnum.Connected.name();
        if (StringUtils.isBlank(this.lastUpdated)) {
            this.lastUpdated = lastUpdated;
            return;
        }
        DateTime parse = DateUtil.parse(lastUpdated);
        DateTime parse1 = DateUtil.parse(this.lastUpdated);
        if (parse == null || parse1 == null ) {
            this.lastUpdated = lastUpdated;
            return;
        }
        if (parse.isAfter(parse1)){
            this.lastUpdated = lastUpdated;
        }
    }
}
