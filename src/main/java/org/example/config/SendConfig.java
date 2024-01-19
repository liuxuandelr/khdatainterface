package org.example.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Data
@Component
public class SendConfig {

    private int mode;

    private static String dataRevHost;

    private static int dataRevPort;

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }


    public static String getDataRevHost() {
        return dataRevHost;
    }
    @Value("${send.host}")
    public static void setDataRevHost(String host) {
        SendConfig.dataRevHost = host;
    }

    public static int getDataRevPort() {
        return dataRevPort;
    }
    @Value("${send.port}")
    public static void setDataRevPort(int port) {
        SendConfig.dataRevPort = port;
    }
}
