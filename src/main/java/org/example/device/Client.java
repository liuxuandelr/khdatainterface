package org.example.device;

import com.beanit.iec61850bean.ClientAssociation;
import com.beanit.iec61850bean.ClientSap;
import com.beanit.iec61850bean.ServiceError;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.example.config.Config;
import org.example.device.config.DeviceConfig;
import org.example.device.config.RcbConfig;
import org.example.device.report.DataReportAcceptor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

@Data
@Slf4j
public class Client {
    public static final int OP_START = 0;
    public static final int OP_RESTART = 1;
    public static final int OP_FORCE_RESTART = 2;
    public static final int OP_EXIT = 100;

    private DataReportAcceptor dataAcceptor;

    private ClientSap clientSap;

    private DeviceConfig deviceConfig;

    private String devName;

    private String devType;

    private List<RcbConfig> rcbConfigList;
    private volatile AtomicInteger reStartCount = new AtomicInteger(0);
    private LinkedBlockingQueue<ClientOperator> startupQueue;
    private Thread startupThread;
    private ClientOperator startupOperator;
    private volatile boolean started = false;
    private volatile boolean restartStatus = false;

    public Client(String devName) {
        this.clientSap = new ClientSap();
        this.clientSap.setMessageFragmentTimeout(60000);
        this.clientSap.setResponseTimeout(180000);
        this.devName = devName;
//        this.deviceConfig = DeviceFactoryImpl.getDeviceConfigs().get(devName);
        this.rcbConfigList = new ArrayList<>(this.deviceConfig.getBrcbs());
        this.rcbConfigList.addAll(this.deviceConfig.getUrcbs());
        this.devType = devName.split("_")[0];
        this.startupQueue = new LinkedBlockingQueue<>();
        this.startupOperator = null;
        this.dataAcceptor = null;
    }

    public Client(DeviceConfig deviceConfig) {
        this.clientSap = new ClientSap();
        this.clientSap.setMessageFragmentTimeout(60000);
        this.clientSap.setResponseTimeout(180000);
        this.devName = deviceConfig.getDeviceName();
        this.deviceConfig = deviceConfig;
        this.rcbConfigList = new ArrayList<>(deviceConfig.getBrcbs());
        this.rcbConfigList.addAll(deviceConfig.getUrcbs());
        this.devType = deviceConfig.getDeviceType();
        this.startupQueue = new LinkedBlockingQueue<>();
        this.startupOperator = null;
        this.dataAcceptor = null;
    }

    public ClientSap getClientSap() {
        return this.clientSap;
    }

    public ClientAssociation getClientAssociation() {
        if (this.dataAcceptor == null) {
            return null;
        }
        return this.dataAcceptor.getClientAssociation();
    }

    public ClientAssociation refreshClientAssociation() {
        if (this.getClientAssociation() == null) {
            return null;
        }
        return this.getDataAcceptor().refreshClientAssociation();
    }

    public void start() {
        this.restartStatus = false;
        this.started = false;
        this.createStartupThread();
        this.startupQueue.offer(new ClientOperator(OP_START, new Date().getTime()));
    }

    /**
     * 数据召唤
     */
//    public void callData(Map<String, Object> commond) throws ServiceError, IOException {
//        dataAcceptor.callData(commond);
//    }

    public boolean getRestartStatus() {
        return restartStatus;
    }

    public void reStart(boolean force) {
        if (!this.restartStatus && (this.dataAcceptor == null || !this.dataAcceptor.getShutdownFlag())) {
            log.info("client重启命令: {}, {}, {}, {}", this.devName, force, new Date().getTime());
            this.startupQueue.offer(
                new ClientOperator(force ? OP_FORCE_RESTART : OP_RESTART, new Date().getTime()));
        }
    }

    public String getDevName() {
        return this.devName;
    }

    public String getDevType() {
        return this.devType;
    }

    public void destroy() {
        dataAcceptorDestroy();
        this.started = false;
        this.restartStatus = false;
        this.startupThread.interrupt();
        this.startupThread.stop();
    }

    public void dataAcceptorDestroy() {
        if (this.dataAcceptor != null) {
            this.dataAcceptor.shutdown();
        }
        this.started = false;
    }

    public List<RcbConfig> getAllRcbConfig() {
        return this.rcbConfigList;
    }

    @Override
    public String toString() {
        return "Client{" +
            "dataAcceptor=" + dataAcceptor +
            ", clientSap=" + clientSap +
            ", clientAssociation=" + this.getClientAssociation() +
            ", deviceConfig=" + deviceConfig +
            ", devName='" + devName + '\'' +
            ", devType='" + devType + '\'' +
            '}';
    }

    public void setStarted(boolean started) {
        this.started = started;
    }

    public boolean getStarted() {
        return this.started;
    }

    public void createStartupThread() {
        this.startupThread = new StartupThread(this);
        this.startupThread.start();
    }

    @Data
    @AllArgsConstructor
    public static class ClientOperator {
        private int opType;
        private long opTime;
    }

    public static class StartupThread extends Thread {
        private Client client;

        public StartupThread(Client client) {
            this.client = client;
        }

        @Override
        public void run() {
            log.info("client启动线程开始工作");
            while (!isInterrupted()) {
                try {
                    ClientOperator operator = client.startupQueue.take();
                    if (operator.opType == OP_EXIT) {
                        log.info("client启动线程结束工作");
                        return;
                    }

                    if (client.startupOperator != null
                        && operator.getOpTime() - client.startupOperator.getOpTime() < Config.getFilePeriod() * 1000) {
                        log.info("client启动忽略频繁重启操作: {}, {}, {}, {}",
                            this.getName(), client.devName, operator.getOpType(),
                            operator.getOpTime());
                        continue;
                    }

                    log.info("client获取启动命令: {}, {}, {}, {}",
                        this.getName(), client.devName, operator.getOpType(),
                        operator.getOpTime());

                    client.restartStatus = true;
                    client.started = false;
                    client.startupOperator = operator;
                    while (!isInterrupted()) {
                        log.info("client启动操作: {}, {}, {}, {}",
                            this.getName(), client.devName, operator.getOpType(),
                            operator.getOpTime());
                        try {
                            if (client.dataAcceptor != null) {
                                client.dataAcceptor.shutdown();
                                client.dataAcceptor.interrupt();
                                client.dataAcceptor.stop();
                            }
                        } catch (Exception e) {
                            log.error("client销毁异常: {}, {}", client.devName, e.getMessage());
                        }
                        client.dataAcceptor = new DataReportAcceptor(client);
                        client.dataAcceptor.start();
//                        if (client.dataAcceptor.waitForStartup()) {
//                            log.info("client启动成功: {}, {}", client.devName,
//                                client.dataAcceptor.getDeviceConfig().getHost());
//                            break;
//                        }
                        try {
                            Thread.sleep(Config.getFilePeriod() * 1000);
                        } catch (Exception e) {
                        }
                    }
                    client.restartStatus = false;
                } catch (Exception e) {
                    log.error("获取启动操作异常：", e);
                }
            }
        }
    }
}
