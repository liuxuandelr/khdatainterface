package org.example.config;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.example.client.LongSocketSender;
import org.example.device.config.DeviceConfig;
import org.example.pojo.SensorEntity;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@Configuration
@Slf4j
public class Config {
    private static final int fileSaveDayNum = -1;
    private static SendConfig sendConfig;

    private static LongSocketSender socketSender;
    private static SafeProperties properties;

    private static String filePath;

    private static List<SensorEntity> sensorEntityList;

    private static boolean localModelICD = false;

    public static Boolean localLogDebug;

    private static int filePeriod = 300;

    private static String localWaveFileVersion = "2";

    //1: 文件路径（已验证莫克服务）; 2：带目录绝对路径; 3: 带目录相对路径
    private static int wavfileDownloadMethod = 2;

    private static Map<String, Map<String, String>> dataInfo = new HashMap<>();

    private static List<String> ownerRcbs = new ArrayList<>();
    public static void init() throws Exception{
        properties = loadProperties("config.properties");
        initSensor();
        initSendConfig(properties);
        sendConfig.setMode(1);
        if (sendConfig.getMode() == 1) {
            socketSender = new LongSocketSender(sendConfig.getDataRevHost(),
                    sendConfig.getDataRevPort(), DataType.POINT_DATA);
            log.info("[socketSender]: {}-{}", sendConfig.getDataRevHost(), sendConfig.getDataRevPort());
            socketSender.start();
        } else {
            socketSender = null;
        }

    }



    public static void initSensor() throws IOException {
        String basePath = System.getProperty("user.dir");
        File file = new File(basePath+File.separator+"config.json");
//        File file = new File("D:\\dataOrigin\\kehdatainterface\\src\\main\\resources\\config.json");
        String file1 = FileUtils.readFileToString(file);
        List<SensorEntity> jsonobject = JSON.parseArray(file1,SensorEntity.class);
        sensorEntityList = jsonobject;
    }
    public static LongSocketSender getSocketSender() {
        return socketSender;
    }
    public static List<SensorEntity> sensorEntityList() {
        return sensorEntityList;
    }

    private static void initSendConfig(Properties properties) {
        try {
            sendConfig = new SendConfig();
            sendConfig.setDataRevHost(properties.getProperty("DATA_REV_HOST").trim());
            sendConfig.setDataRevPort(Integer.parseInt(properties.getProperty("DATA_REV_PORT").trim()));
            String mode = properties.getProperty("SEND_MODE");
            sendConfig.setMode(0);
            if (StringUtils.isNoneBlank(mode)) {
                sendConfig.setMode(1);
            }
            log.info("加载发送配置完成:" + sendConfig);
        } catch (Exception ex) {
            log.error("发送配置读取异常", ex);
            throw ex;
        }
    }
    public static SafeProperties loadProperties(String fileName) throws IOException {
        InputStream fis = null;
        SafeProperties prop = new SafeProperties();
        // 获取程序当前路径
        String basePath = System.getProperty("user.dir");
        try {
            String configFilePath =basePath+File.separator+"config.properties";
//            String configFilePath ="D:\\dataOrigin\\kehdatainterface\\src\\main\\resources\\config.properties";
            // exists判断配置文件是否存在
            if (new File(configFilePath).exists()) {
                filePath = configFilePath;
                fis = new FileInputStream(configFilePath);
            } else {
                ClassPathResource cpr = new ClassPathResource(fileName);
                filePath = cpr.getPath();
                fis = cpr.getInputStream();
            }
            prop.load(fis);
            log.info("加载配置文件:" + filePath);
        } catch (IOException ex) {
            log.error("读取配置文件出错", ex);
            throw ex;
        } finally {
            if (fis != null)
                fis.close();
        }

        return prop;
    }

    public static int getFileSaveDayNum() {
        return fileSaveDayNum;
    }

    public static boolean getLocalICD() {
        return localModelICD;
    }

//    public static Map<String, DeviceConfig> getDeviceConfigs() {
//        return DeviceFactoryImpl.getDeviceConfigs();
//    }
    public static int getFilePeriod() {
        return filePeriod;
    }

    public static String getLocalWaveFileVersion() {
        return localWaveFileVersion;
    }

    public static int getWavfileDownloadMethod() {
        return wavfileDownloadMethod;
    }

    public static Map<String, Map<String, String>> getDataInfo() {
        return dataInfo;
    }

    public static List<String> getOwnerRcbs() {
        return ownerRcbs;
    }
}
