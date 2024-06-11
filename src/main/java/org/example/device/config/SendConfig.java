package org.example.device.config;

public class SendConfig {
    private int dataSndPort;

    private String dataRevHost;

    private int dataRevPort;

    private long dataPeriod;

    private int heartbeatPeriod;

    private String kafkaConfig;

    private int mode;

    public String getDataRevHost() {
        return dataRevHost;
    }

    public void setDataRevHost(String dataRevHost) {
        this.dataRevHost = dataRevHost;
    }

    public int getDataRevPort() {
        return dataRevPort;
    }

    public void setDataRevPort(int dataRevPort) {
        this.dataRevPort = dataRevPort;
    }

    public int getDataSndPort() {
        return dataSndPort;
    }

    public void setDataSndPort(int dataSndPort) {
        this.dataSndPort = dataSndPort;
    }

    public long getDataPeriod() {
        return dataPeriod;
    }

    public void setDataPeriod(long dataPeriod) {
        this.dataPeriod = dataPeriod;
    }

    public int getHeartbeatPeriod() {
        return heartbeatPeriod;
    }

    public void setHeartbeatPeriod(int heartbeatPeriod) {
        this.heartbeatPeriod = heartbeatPeriod;
    }

    public String getKafkaConfig() {
        return kafkaConfig;
    }

    public void setKafkaConfig(String kafkaConfig) {
        this.kafkaConfig = kafkaConfig;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public int getMode() {
        return this.mode;
    }

    @Override
    public String toString() {
        return "SendConfig [dataRevHost=" + dataRevHost + ", dataRevPort=" + dataRevPort + ", dataPeriod="
            + dataPeriod + ", heartbeatPeriod=" + heartbeatPeriod + "]";
    }

}
