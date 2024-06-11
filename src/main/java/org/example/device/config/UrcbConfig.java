package org.example.device.config;

import com.beanit.iec61850bean.*;
import lombok.extern.slf4j.Slf4j;
import org.example.device.entity.DeviceRcb;
import org.example.device.report.DataReportAcceptor;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Slf4j
public class UrcbConfig extends RcbConfig {

    public UrcbConfig() {
    }

    public UrcbConfig(String ref, boolean datachange, boolean dataupdate, boolean qualityChange, boolean integrity,
        boolean generalInterrogation, long intgPd) {
        this.ref = ref;
        this.datachange = datachange;
        this.dataupdate = dataupdate;
        this.qualityChange = qualityChange;
        this.integrity = integrity;
        this.generalInterrogation = generalInterrogation;
        this.intgPd = intgPd;
    }

    public String toString() {
        return "UrcbConfig{ref='" + this.ref + '\'' + ", datachange=" + this.datachange + ", dataupdate=" + this.dataupdate + ", qualityChange=" + this.qualityChange + ", integrity=" + this.integrity + ", generalInterrogation=" + this.generalInterrogation + ", intgPd=" + this.intgPd + '}';
    }

    @Override
    public int enableRcb(DataReportAcceptor dataReportAcceptor, String rcbRef, DeviceRcb deviceRcb) {
        Urcb rcb = dataReportAcceptor.getServerModel().getUrcb(rcbRef);
        if (rcb == null) {
            log.error("使能UrcbRef不存在not exist：{}, {}", dataReportAcceptor.getDeviceConfig().getDeviceTypeDes(), this.getRef());
            log.error("请使用正确的rcbRef：{}", dataReportAcceptor.getDeviceRcb().getAllUrcbNameList());
            return 0;
        }

        Urcb selectedRcb = null;
        // 必须调佣获取 rcb 的值，否则无法感知可用状态
        try {
            dataReportAcceptor.getClientAssociation().getRcbValues(rcb);
        } catch (Exception e) {
            log.info("获取使能接口信息异常: " + rcb.getReference() + ">> " + e.getMessage());
        }
        // 判断使能接口可用状态 true 接口冲突  / false 接口可用
        ObjectReference reference = rcb.getReference();
        log.info("开始使能: {}", reference);
        String devRef = "";
        String urcbRefPre = "";
        devRef = rcb.getParent().getReference().toString();
        urcbRefPre = rcb.getReference().toString();
        urcbRefPre = urcbRefPre.substring(0, urcbRefPre.length() - 2);
        Map<String, List<Urcb>> map = dataReportAcceptor.getDeviceRcb().getAllUrcbMapList().get(devRef);
        if (map == null) {
            log.info("使能项目未找到: {}", reference);
            return 0;
        }
        List<Urcb> list = map.get(urcbRefPre);
        int seedIdx = deviceRcb.getRefEnableSeed(urcbRefPre);
        if (seedIdx == -1) {
            for (int n = 0; n < list.size(); ++n) {
                Urcb urcb = list.get(n);
                if (urcb.getReference().toString().equals(rcb.getReference().toString())) {
                    seedIdx = n;
                    break;
                }
            }
        }

        if (seedIdx >= list.size() - 1) {
            seedIdx = 0;
        }

        for (int n = seedIdx; n < list.size(); ++n) {
            if (list.size() > RcbConfig.BEGIN_IDX + 1 && n < RcbConfig.BEGIN_IDX) {
                continue;
            }
            Urcb urcb = list.get(n);
            try {
                dataReportAcceptor.getClientAssociation().reserveUrcb(urcb);
                selectedRcb = urcb;
                seedIdx = n;
                break;
            } catch (Exception e) {
                log.error("URCB reserveUrcb fail: {}, {}", urcb.getReference(), e.getMessage());
            }
        }

        if (selectedRcb == null) {
            for (int n = 0; n < seedIdx; ++n) {
                if (list.size() > RcbConfig.BEGIN_IDX + 1 && n < RcbConfig.BEGIN_IDX) {
                    continue;
                }
                Urcb urcb = list.get(n);
                try {
                    dataReportAcceptor.getClientAssociation().reserveUrcb(urcb);
                    selectedRcb = urcb;
                    seedIdx = n;
                } catch (Exception e) {
                    log.error("URCB reserveUrcb fail: {}, {}", urcb.getReference(), e.getMessage());
                }
            }
        }

        if (selectedRcb == null) {
            selectedRcb = rcb;
            log.info("RptEna not found: {}", urcbRefPre);
//            LogReport.report("RptEna not found: " + urcbRefPre, DataType.ERROR_INFO);
        } else {
            log.info("RptEna found: {}", selectedRcb.getReference().toString());
            deviceRcb.setRefEnableSeed(urcbRefPre, seedIdx);
        }

        this.setNewRcdRef(selectedRcb.getReference().toString());

        try {
            this.enableRcb(dataReportAcceptor, selectedRcb, deviceRcb);
        } catch (IOException | ServiceError e) {
            log.error("使能失败: {}, ", selectedRcb.getReference(), e);
//            LogReport.report("RptEna failure: " + selectedRcb.getReference(), DataType.ERROR_INFO);
            return 0;
        }

        this.setUpdate(true);
        return 1;
    }

    @Override
    public void enableRcb(DataReportAcceptor dataReportAcceptor, Rcb rcb,
        DeviceRcb deviceRcb) throws IOException, ServiceError {
        Urcb urcb = (Urcb) rcb;
        BdaTriggerConditions triggerOptions = urcb.getTrgOps();
        triggerOptions.setDataChange(this.getDatachange());
        triggerOptions.setDataUpdate(this.getDataupdate());
        triggerOptions.setQualityChange(this.getQualityChange());
        triggerOptions.setIntegrity(this.getIntegrity());
        triggerOptions.setGeneralInterrogation(this.getGeneralInterrogation());
        if (this.getIntegrity())
            urcb.getIntgPd().setValue(this.getIntgPd());
        dataReportAcceptor.getClientAssociation().setRcbValues(urcb, false, false, false,
            false, true, this.getIntegrity(), false, false);
        dataReportAcceptor.getClientAssociation().enableReporting(urcb);
        dataReportAcceptor.getEnabledUrcbs().add(urcb);
        log.info("RptEna active: {}", rcb.getReference().toString());
    }
}
