package org.example.device.config;

import com.beanit.iec61850bean.Rcb;
import com.beanit.iec61850bean.ServiceError;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.example.device.entity.DeviceRcb;
import org.example.device.report.DataReportAcceptor;

import java.io.IOException;

@NoArgsConstructor
@Setter
@Getter
@Slf4j
public abstract class RcbConfig {
    public static final int BEGIN_IDX = 4;
    String ref;

    boolean datachange = true;

    boolean dataupdate = false;

    boolean qualityChange = false;

    boolean integrity = true;

    boolean generalInterrogation = true;

    boolean purgeBufValue = true;

    long bufTmValue;

    long intgPd;

    String newRcdRef;

    String preRcdRef;

    Rcb rcb;

    int changeCount = 0;

    boolean isUpdate = false;

    int rcbIndex = 0;

    public boolean isUpdate() {
        return isUpdate;
    }

    public void setUpdate(boolean update) {
        isUpdate = update;
    }

    public String getNewRcdRef() {
        if (StringUtils.isBlank(newRcdRef)) {
            newRcdRef = ref;
        }
        return newRcdRef;
    }

    public void setNewRcdRef(String newRcdRef) {
        this.ref = newRcdRef;
        this.newRcdRef = newRcdRef;
    }

    public String getRef() {
        return this.ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public boolean getDatachange() {
        return this.datachange;
    }

    public void setDatachange(boolean datachange) {
        this.datachange = datachange;
    }

    public boolean getDataupdate() {
        return this.dataupdate;
    }

    public void setDataupdate(boolean dataupdate) {
        this.dataupdate = dataupdate;
    }

    public boolean getQualityChange() {
        return this.qualityChange;
    }

    public void setQualityChange(boolean qualityChange) {
        this.qualityChange = qualityChange;
    }

    public boolean getIntegrity() {
        return this.integrity;
    }

    public void setIntegrity(boolean integrity) {
        this.integrity = integrity;
    }

    public boolean getGeneralInterrogation() {
        return this.generalInterrogation;
    }

    public void setGeneralInterrogation(boolean generalInterrogation) {
        this.generalInterrogation = generalInterrogation;
    }

    public boolean getPurgeBufValue() {
        return this.purgeBufValue;
    }

    public void setPurgeBufValue(boolean purgeBufValue) {
        this.purgeBufValue = purgeBufValue;
    }

    public long getBufTmValue() {
        return this.bufTmValue;
    }

    public void setBufTmValue(long bufTmValue) {
        this.bufTmValue = bufTmValue;
    }

    public long getIntgPd() {
        return this.intgPd;
    }

    public void setIntgPd(long intgPd) {
        this.intgPd = intgPd;
    }

    abstract public void enableRcb(DataReportAcceptor dataReportAcceptor, Rcb rcb,
                                   DeviceRcb deviceRcb) throws IOException, ServiceError;

    abstract public int enableRcb(DataReportAcceptor dataReportAcceptor, String rcbRef, DeviceRcb deviceRcb);
}
