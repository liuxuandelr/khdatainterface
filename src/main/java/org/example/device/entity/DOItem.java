package org.example.device.entity;

import cn.hutool.core.date.DateTime;
import com.beanit.iec61850bean.BasicDataAttribute;

import java.util.List;

public class DOItem {
    private String ref;

    private String destAttr;

    private String srcAttr;

    private String desc;

    private List<BasicDataAttribute> dataAttributes;

    private DateTime maxAttrDate;

    private boolean watched = false;

    private String value;

    public DateTime getMaxAttrDate() {
        return maxAttrDate;
    }

    public void setMaxAttrDate(DateTime maxAttrDate) {
        this.maxAttrDate = maxAttrDate;
    }

    public String getRef() {
        return this.ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public String getDestAttr() {
        return this.destAttr;
    }

    public void setDestAttr(String destAttr) {
        this.destAttr = destAttr;
    }

    public String getSrcAttr() {
        return this.srcAttr;
    }

    public void setSrcAttr(String srcAttr) {
        this.srcAttr = srcAttr;
    }

    public String getDesc() {
        return this.desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public List<BasicDataAttribute> getDataAttributes() {
        return this.dataAttributes;
    }

    public void setDataAttributes(List<BasicDataAttribute> dataAttributes) {
        this.dataAttributes = dataAttributes;
    }

    public boolean isWatched() {
        return watched;
    }

    public void setWatched(boolean watched) {
        this.watched = watched;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "DOItem{" +
                "ref='" + ref + '\'' +
                ", destAttr='" + destAttr + '\'' +
                ", srcAttr='" + srcAttr + '\'' +
                ", desc='" + desc + '\'' +
                ", dataAttributes=" + dataAttributes +
                '}';
    }
}
