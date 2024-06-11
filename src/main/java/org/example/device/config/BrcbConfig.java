package org.example.device.config;

import com.beanit.iec61850bean.*;
import lombok.extern.slf4j.Slf4j;
import org.example.config.DataType;
import org.example.device.entity.DeviceRcb;
import org.example.device.report.DataReportAcceptor;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Slf4j
public class BrcbConfig extends RcbConfig {

    public BrcbConfig(String ref, boolean datachange, boolean dataupdate, boolean qualityChange, boolean integrity,
        boolean generalInterrogation, boolean purgeBufValue, long bufTmValue, long intgPd) {
        this.ref = ref;
        this.datachange = datachange;
        this.dataupdate = dataupdate;
        this.qualityChange = qualityChange;
        this.integrity = integrity;
        this.generalInterrogation = generalInterrogation;
        this.purgeBufValue = purgeBufValue;
        this.bufTmValue = bufTmValue;
        this.intgPd = intgPd;
    }

    public BrcbConfig() {
    }

    public String toString() {
        return "BrcbConfig{ref='" + this.ref + '\'' + ", datachange=" + this.datachange + ", dataupdate=" + this.dataupdate + ", qualityChange=" + this.qualityChange + ", integrity=" + this.integrity + ", generalInterrogation=" + this.generalInterrogation + ", purgeBufValue=" + this.purgeBufValue + ", bufTmValue=" + this.bufTmValue + ", intgPd=" + this.intgPd + '}';
    }

    @Override
    public int enableRcb(DataReportAcceptor dataReportAcceptor, String rcbRef, DeviceRcb deviceRcb) {
        Brcb rcb = dataReportAcceptor.getServerModel().getBrcb(rcbRef);
        if (rcb == null) {
            log.error("使能BrcbRef不存在not exist：{}, {}", dataReportAcceptor.getDeviceConfig().getDeviceTypeDes(), this.getRef());
            log.error("请使用正确的rcbRef：{}", dataReportAcceptor.getDeviceRcb().getAllBrcbNameList());
            return 0;
        }

        // 必须调佣获取 rcb 的值，否则无法感知可用状态
        try {
            dataReportAcceptor.getClientAssociation().getRcbValues(rcb);
        } catch (Exception e) {
            log.error("获取使能状态失败，", e);
        }

        // 判断使能接口可用状态 true 接口冲突  / false 接口可用
        ObjectReference reference = rcb.getReference();
        log.info("开始使能: {}", reference);
        String devRef = "";
        String rcbRefPre = "";
        devRef = rcb.getParent().getReference().toString();
        rcbRefPre = rcb.getReference().toString();
        rcbRefPre = rcbRefPre.substring(0, rcbRefPre.length() - 2);
        Map<String, List<Brcb>> map = dataReportAcceptor.getDeviceRcb().getAllBrcbMapList().get(devRef);
        if (map == null) {
            log.info("使能项目未找到: {}", reference);
            return 0;
        }
        List<Brcb> list = map.get(rcbRefPre);
        if (list.isEmpty()) {
            return 0;
        }

        Brcb selectedRcb = null;
        int seedIdx = deviceRcb.getRefEnableSeed(rcbRefPre);
        if (seedIdx == -1) {
            for (int n = 0; n < list.size(); ++n) {
                Brcb brcb = list.get(n);
                if (brcb.getReference().toString().equals(rcb.getReference().toString())) {
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
            Brcb brcb = list.get(n);
            try {
                dataReportAcceptor.getClientAssociation().getRcbValues(brcb);
                if (brcb.getResvTms() != null) {
                    dataReportAcceptor.getClientAssociation().reserveBrcb(brcb,
                        (short) (this.getBufTmValue() / 1000));
                } else if (brcb.getRptEna().getValue()) {
                    continue;
                }
                selectedRcb = brcb;
                seedIdx = n;
                break;
            } catch (Exception e) {
                log.error("getRptEna fail: {}, ", brcb.getReference(), e);
            }
        }

        if (selectedRcb == null) {
            for (int n = 0; n < seedIdx; ++n) {
                if (list.size() > RcbConfig.BEGIN_IDX + 1 && n < RcbConfig.BEGIN_IDX) {
                    continue;
                }
                Brcb brcb = list.get(n);
                try {
                    dataReportAcceptor.getClientAssociation().getRcbValues(brcb);
                    if (brcb.getResvTms() != null) {
                        dataReportAcceptor.getClientAssociation().reserveBrcb(brcb,
                            (short) (this.getBufTmValue() / 1000));
                    } else if (brcb.getRptEna().getValue()) {
                        continue;
                    }
                    selectedRcb = brcb;
                    seedIdx = n;
                    break;
                } catch (Exception e) {
                    log.error("getRptEna fail: {}, ", brcb.getReference(), e);
                }
            }
        }

        if (selectedRcb == null) {
            log.info("RptEna not found: {}", rcbRefPre);
//            LogReport.report("RptEna not found: " + rcbRefPre, DataType.ERROR_INFO);
            selectedRcb = rcb;
        } else {
            String selectedRcbRef = selectedRcb.getReference().toString();
            log.info("RptEna found: {}", selectedRcbRef);
            deviceRcb.setRefEnableSeed(rcbRefPre, seedIdx);
        }

        this.setNewRcdRef(selectedRcb.getReference().toString());

        try {
            enableRcb(dataReportAcceptor, selectedRcb, deviceRcb);
        } catch (IOException | ServiceError e) {
            log.error("使能失败: {}, ", selectedRcb.getReference(), e);
//            LogReport.report("RptEna failure: " + selectedRcb.getReference(), DataType.ERROR_INFO);
            return 0;
        }
        return 1;
    }

    public void enableRcb(DataReportAcceptor dataReportAcceptor, Rcb rcb,
        DeviceRcb deviceRcb) throws IOException, ServiceError {
        Brcb brcb = (Brcb) rcb;
        BdaTriggerConditions triggerOptions = brcb.getTrgOps();
        triggerOptions.setDataChange(this.getDatachange());
        triggerOptions.setDataUpdate(this.getDataupdate());
        triggerOptions.setQualityChange(this.getQualityChange());
        triggerOptions.setIntegrity(this.getIntegrity());
        triggerOptions.setGeneralInterrogation(this.getGeneralInterrogation());
        if (this.getIntegrity())
            brcb.getIntgPd().setValue(this.getIntgPd());
        brcb.getPurgeBuf().setValue(this.getPurgeBufValue());
        brcb.getBufTm().setValue(this.getBufTmValue());
        dataReportAcceptor.getClientAssociation().setRcbValues(brcb, false, false, false,
            true, true, this.getIntegrity(), true, false);
        dataReportAcceptor.getClientAssociation().enableReporting(brcb);
        dataReportAcceptor.getEnabledBrcbs().add(brcb);
        log.info("RptEna active: {}", rcb.getReference().toString());
    }

}
