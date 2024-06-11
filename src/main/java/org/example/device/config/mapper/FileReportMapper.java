package org.example.device.config.mapper;

public class FileReportMapper {

    /**
     * 生成事件文件的时间
     */
    private String waveFileSTime;

    private String waveFileSTimeRef;
    /**
     * 生成事件文件的通道
     */
    private String waveFileCoupler;

    private String waveFileCouplerRef;

    public String getWaveFileSTime() {
        return waveFileSTime;
    }

    public void setWaveFileSTime(String waveFileSTime) {
        this.waveFileSTime = waveFileSTime;
    }

    public String getWaveFileCoupler() {
        return waveFileCoupler;
    }

    public void setWaveFileCoupler(String waveFileCoupler) {
        this.waveFileCoupler = waveFileCoupler;
    }

    public String getWaveFileSTimeRef() {
        return waveFileSTimeRef;
    }

    public void setWaveFileSTimeRef(String waveFileSTimeRef) {
        this.waveFileSTimeRef = waveFileSTimeRef;
    }

    public String getWaveFileCouplerRef() {
        return waveFileCouplerRef;
    }

    public void setWaveFileCouplerRef(String waveFileCouplerRef) {
        this.waveFileCouplerRef = waveFileCouplerRef;
    }

    @Override
    public String toString() {
        return "FileReportMapper [waveFileSTime=" + waveFileSTime + ", waveFileSTimeRef=" + waveFileSTimeRef
                + ", waveFileCoupler=" + waveFileCoupler + ", waveFileCouplerRef=" + waveFileCouplerRef + "]";
    }

}
