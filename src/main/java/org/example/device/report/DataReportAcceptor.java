package org.example.device.report;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.beanit.iec61850bean.*;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.example.config.Config;
import org.example.config.DataType;
import org.example.device.Client;
import org.example.device.config.BrcbConfig;
import org.example.device.config.DeviceConfig;
import org.example.device.config.UrcbConfig;
import org.example.device.config.enable.DevTool;
import org.example.device.entity.*;
import org.example.device.report.wavefile.WavePackParser;
import org.example.utils.ByteUtil;
import org.example.utils.ReportUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

@Data
@Slf4j
public class DataReportAcceptor extends Thread implements ClientEventListener {
    private static final Logger logger = LoggerFactory.getLogger(DataReportAcceptor.class);

    private ClientAssociation clientAssociation = null;
    private ClientAssociation fileClientAssociation = null;

    private ServerModel serverModel;

    private List<LNDevice> lnDevices;

    private List<String> reportRefs;

    private List<Brcb> enabledBrcbs;
    private List<Urcb> enabledUrcbs;

    private DeviceRcb deviceRcb;

    private Map<String, Integer> refDeviceMapper;

    private ScheduledExecutorService heartBeatExecutorService = new ScheduledThreadPoolExecutor(1);

    private ScheduledExecutorService networkCheckExecutorService = new ScheduledThreadPoolExecutor(1);

    private Client client;

    private DeviceConfig deviceConfig;

    // 最新数据接收时间
    private AtomicLong lastUpdated;

    // 最新数据上报时间
    private AtomicLong lastDataTime;

    // 最新的有效数据更新时间
    private AtomicLong lastUpdateDataTime;

    private FileReporterInfo info;

    private Map<String, Long> lastUpdatedSensors;

    private Map<String, Long> lastAlarmedSensors;

    private Map<String, String> lastUpdatedAttrs;

    private DownloadFileThread downloadFileThread;
    private boolean shutdownFlag = false;
    private boolean started = false;

    private CountDownLatch countDownLatch;

    public static List<String> KEY_ATTRS = new ArrayList<>();

    static {
        KEY_ATTRS.add("LSCAmp");
        KEY_ATTRS.add("H");
        KEY_ATTRS.add("DschCnt");
    }

    public DataReportAcceptor(Client client) {
        this.setName(client.getDevName());
        this.client = client;
//        this.deviceConfig = Config.getDeviceConfigs().get(client.getDevName());
        this.enabledBrcbs = new ArrayList<>();
        this.enabledUrcbs = new ArrayList<>();
        this.lastUpdated = new AtomicLong(System.currentTimeMillis());
        this.lastDataTime = new AtomicLong(System.currentTimeMillis());
        this.lastUpdateDataTime = new AtomicLong(System.currentTimeMillis());
        this.lastUpdatedSensors = new HashMap<>();
        this.lastAlarmedSensors = new HashMap<>();
        this.lastUpdatedAttrs = new HashMap<>();
        this.info = new FileReporterInfo();
        this.clientAssociation = null;
        this.countDownLatch = new CountDownLatch(1);
    }


    public FileReporterInfo getInfo() {
        return info;
    }

    public synchronized ClientAssociation refreshClientAssociation() {
        if (this.clientAssociation != null && this.clientAssociation.isOpen()) {
            return this.clientAssociation;
        }
        try {
            this.clientAssociation = this.client.getClientSap().associate(
                InetAddress.getByName(this.deviceConfig.getHost()), this.deviceConfig.getPort(),
                null, this);
        } catch (Exception e) {
            logger.error("CLIENT-WRONG: {}-{}:{}, ", this.getName(),
                this.deviceConfig.getHost(), this.deviceConfig.getPort(), e);
            if (!this.shutdownFlag) {
                this.client.reStart(true);
            }
            return null;
        }
        return this.clientAssociation;
    }

    public synchronized ClientAssociation refreshFileClientAssociation() {
        if (this.fileClientAssociation != null && this.fileClientAssociation.isOpen()) {
            return this.fileClientAssociation;
        }
        try {
            this.fileClientAssociation = this.client.getClientSap().associate(
                InetAddress.getByName(this.deviceConfig.getHost()), this.deviceConfig.getPort(),
                null, this);
        } catch (Exception e) {
            logger.error("FILE-CLIENT-WRONG: {}-{}:{}, ", this.getName(),
                this.deviceConfig.getHost(), this.deviceConfig.getPort(), e);
            return null;
        }
        return this.fileClientAssociation;
    }

    @Override
    public void run() {
        this.shutdownFlag = false;
        this.started = false;

        if (!loadServerModelInit()) {
            logger.error(deviceConfig.getDeviceName() + ">>配置加载失败");
            this.countDownLatch.countDown();
            return;
        }

//        DeviceInfo deviceInfo = CollectContextImpl.getDeviceInfo(this.client.getDevName());
//        deviceInfo.againInitDeviceInfo();

        try {
            enableReportUrcbs();
            enableReportBrcbs();
        } catch (Exception ex) {
            logger.error(deviceConfig.getDeviceName() + ">>数据采集模块使能接口初始化失败: ", ex);
        }

        if (this.deviceConfig.isHasFile()) {
            refreshFileClientAssociation();
            initFileReportInfo();
            startGetFileTask();
        }

        if (Config.getLocalICD()) {
            this.clientAssociation.setServerModel(serverModel);
        }

//        CollectContextImpl.getDeviceInfoMap().get(this.client.getDevName()).setInitStatus(true);
//        MyDeviceListener.setDeviceStatus(this.client.getDevName(), DEVICE_STATUS.normal);
        this.started = true;
        this.countDownLatch.countDown();
    }

    private boolean loadServerModelInit() {
        DeviceSclParser csp = new DeviceSclParser(this.client.getDevType(), this.client.getDevName());
        String basePath = System.getProperty("user.dir");
        String icdFilePath = basePath + File.separator + "icd" + File.separator
            + this.deviceConfig.getDeviceType() + File.separator + this.deviceConfig.getIcdName();
        logger.info("加载ICD文件:" + icdFilePath);
        try {
            if ((new File(icdFilePath)).exists()) {
                this.serverModel = csp.parse(icdFilePath).get(0);
            } else {
                ClassPathResource cpr = new ClassPathResource(this.deviceConfig.getIcdName());
                icdFilePath = cpr.getURL().toString();
                this.serverModel = csp.parse(cpr.getInputStream()).get(0);
            }
        } catch (Exception e) {
            logger.error("加载" + this.deviceConfig.getDeviceTypeDes() + "ICD文件:" + icdFilePath + "出错", e);
            return false;
        }
        this.lnDevices = csp.getLnDevices();
        if (this.lnDevices.size() == 0) {
            logger.error(this.deviceConfig.getDeviceTypeDes() + "ip:" + this.deviceConfig.getHost()
                + "上没有找到需要上报的设备");
            return false;
        }
        this.reportRefs = csp.getReportRefs();
        for (String ref : this.reportRefs) {
            log.debug("ReportRefs：{}", ref);
        }
        this.refDeviceMapper = csp.getRefDeviceMapper();
        logger.info(String.format(this.deviceConfig.getDeviceType() + this.deviceConfig.getHost()
            + "上找到 %s 个需要上报的设备，共 %s 个数据", this.lnDevices.size(), this.reportRefs.size()));
        if (null == refreshClientAssociation()) {
            return false;
        }
        logger.info("成功获取连接: {}, {}:{}", this.deviceConfig.getDeviceType(), this.deviceConfig.getHost(),
            this.deviceConfig.getPort());
        try {
            ServerModel tmpModel = this.clientAssociation.retrieveModel();
            if (!Config.getLocalICD()) {
                this.serverModel = tmpModel;
            }
            logger.info("SERVER: {}-{}", this.deviceConfig.getDeviceType(), this.deviceConfig.getHost());
        } catch (Exception e) {
            logger.warn("RetrieveModel fail: ", e);
            this.clientAssociation.setServerModel(this.serverModel);
        }
        logger.info("本地缓存数据开始初始化: {}:{}", this.deviceConfig.getDeviceType(),
            this.deviceConfig.getHost());

        //initLocalLNDevicesData();
        //logger.info("本地缓存数据初始化完成: {}, {}", this.deviceConfig.getDeviceType(), this.deviceConfig.getHost());
        loadDeviceRcb(clientAssociation);

        return true;
    }

    public void loadDeviceRcb(ClientAssociation clientAssociation) {
        deviceRcb = new DeviceRcb();
        Map<String, Map<String, List<Brcb>>>[] brcbAr = DevTool.reportEnableFindBrcb(clientAssociation, serverModel);
        Map<String, Map<String, List<Urcb>>>[] UrcbAr = DevTool.reportEnableFindUrcb(clientAssociation, serverModel);
        //deviceRcb.setDeviceRef(DevTool.reportDevRef(clientAssociation));
        deviceRcb.setBrcbMapList(brcbAr[0], brcbAr[1]);
        deviceRcb.setUrcbMapList(UrcbAr[0], UrcbAr[1]);
        deviceRcb.arrangeList();
        deviceRcb.setEnableSeedMap(new HashMap<>());
    }

    public void enableReportUrcbs() {
        List<UrcbConfig> urcbConfigs = this.deviceConfig.getUrcbs();
        int count = 0;
        for (UrcbConfig urcbConfig : urcbConfigs) {
            count = count + urcbConfig.enableRcb(this, urcbConfig.getNewRcdRef(), this.deviceRcb);
        }
        logger.info(String.format(this.deviceConfig.getDeviceTypeDes() + ":" + this.deviceConfig.getHost()
            + "上 urcb %s 个上报组件已启用 %s 个", urcbConfigs.size(), count));
    }

    public void enableReportBrcbs() {
        List<BrcbConfig> brcbConfigs = this.deviceConfig.getBrcbs();
        int count = 0;
        for (BrcbConfig brcbConfig : brcbConfigs) {
            count = count + brcbConfig.enableRcb(this, brcbConfig.getNewRcdRef(), this.deviceRcb);
        }
        logger.info(String.format(this.deviceConfig.getDeviceTypeDes() + ":" + this.deviceConfig.getHost()
            + "上 brcb %s 个上报组件已启用 %s 个", brcbConfigs.size(), count));
    }

    private void initFileReportInfo() {
        this.info = new FileReporterInfo();
        String baseDir = this.deviceConfig.getFileCacheDir();
        this.info.setBaseDir(baseDir);
        this.info.initDownloadDate();
    }

    private void cancelGetFileTask() {
        try {
            if (downloadFileThread != null) {
                downloadFileThread.interrupt();
                downloadFileThread.stop();
                downloadFileThread.setStopped(true);
            }
        } catch (Exception e) {
        }
    }


    public void shutdown() {
        this.shutdownFlag = true;
        try {
            cancelGetFileTask();
            try {
//                if (this.sendGiThread != null) {
//                    this.sendGiThread.interrupt();
//                    this.sendGiThread.stop();
//                    this.sendGiThread.setStopped(true);
//                }
            } catch (Exception e) {
            }
            if (this.clientAssociation != null) {
                this.enabledBrcbs.clear();
                this.enabledUrcbs.clear();
                this.clientAssociation.close();
                this.clientAssociation = null;
            }
            this.serverModel = null;
            this.lnDevices = null;
            this.reportRefs = null;
            this.refDeviceMapper = null;
        } catch (Exception ignored) {
            logger.warn("SHUTDOWN: {}", ignored.getMessage());
        } finally {
            System.gc();
        }
        this.lastUpdatedSensors.clear();
    }

    @Override
    public void newReport(Report report) {
        try {
            long timestampValue;
            try {
                BdaEntryTime timeOfEntry = report.getTimeOfEntry();
                timestampValue = timeOfEntry.getTimestampValue();
            } catch (Exception ignored) {
                timestampValue = System.currentTimeMillis();
            }
            this.lastDataTime.set(timestampValue);

            log.debug(timestampValue + "接收到数据--" + this.deviceConfig.getDeviceTypeDes()
                + "--ip：" + this.deviceConfig.getHost() + ", 条数: " + report.getValues().size());

            log.debug("数据内容: " + report.getValues());

//            List<FcModelNode> fcNodes = report.getValues();
//            this.updateLocal(fcNodes);
        } catch (Exception e) {
            logger.error(this.deviceConfig.getDeviceTypeDes() + ":" + this.deviceConfig.getHost()
                + "接收到新数据，处理数据时异常", e);
        }
    }

    @Override
    public void associationClosed(IOException e) {
        logger.error("associationClosed: " + this.getName() + " - "
            + this.deviceConfig.getDeviceTypeDes() + ":" + this.deviceConfig.getHost()
            + "连接关闭", e);
        if (!this.shutdownFlag) {
            this.client.reStart(false);
        }
    }

    public void callData(Map<String, Object> commond) throws ServiceError, IOException {
        for (LNDevice lnDevice : this.lnDevices) {
            if (commond.get("objid").toString().equals(lnDevice.getSensorid())) {
                List<DOItem> doitems = lnDevice.getDoItems();
                for (DOItem item : doitems) {
                    if (commond.containsKey(item.getDestAttr())) {
                        FcModelNode fcModelNode = (FcModelNode) this.serverModel.findModelNode(item.getRef(), null);
                        this.clientAssociation.getDataValues(fcModelNode);
                        item.setDataAttributes(fcModelNode.getBasicDataAttributes());
                    }
                }
                JSONObject jsonObject = lnDevice.getJsonObject();
                List<Map<String, Object>> list = (List<Map<String, Object>>) jsonObject.get("attrs");
                ListIterator<Map<String, Object>> listIterator = list.listIterator();
                while (listIterator.hasNext()) {
                    Map<String, Object> item = listIterator.next();
                    if (!commond.containsKey(item.get("name")))
                        listIterator.remove();
                }
                jsonObject.put("attrs", list);
                jsonObject.put("type", commond.get("type").toString());
                JSONArray array = new JSONArray();
                array.add(jsonObject);
//                SendExecutor.submit(new DataSender(array, DataType.CALL_DATA));
                break;
            }
        }
    }

    @Override
    public String toString() {
        return "DataReportAcceptor{" +
            "clientAssociation=" + clientAssociation +
            ", serverModel=" + serverModel +
            ", lnDevices=" + lnDevices +
            ", reportRefs=" + reportRefs +
            ", enabledBrcbs=" + enabledBrcbs +
            ", enabledUrcbs=" + enabledUrcbs +
            ", deviceRcb=" + deviceRcb +
            ", refDeviceMapper=" + refDeviceMapper +
            ", heartBeatExecutorService=" + heartBeatExecutorService +
            ", networkCheckExecutorService=" + networkCheckExecutorService +
            ", deviceConfig=" + deviceConfig +
            ", lastUpdated=" + lastUpdated +
            ", lastDataTime=" + lastDataTime +
            ", lastUpdateDataTime=" + lastUpdateDataTime +
            ", info=" + info +
            ", lastUpdatedSensors=" + lastUpdatedSensors +
            '}';
    }

    private void startGetFileTask() {
        downloadFileThread = new DownloadFileThread(this.info);
        downloadFileThread.start();
    }

    private class DownloadFileThread extends Thread {
        private AtomicBoolean working;
        private final FileReporterInfo info;
        private volatile boolean stopped = false;
        private SimpleDateFormat timestampSDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        public DownloadFileThread(FileReporterInfo info) {
            this.info = info;
            this.working = new AtomicBoolean();
            this.working.set(false);
        }

        public void setStopped(boolean stopped) {
            this.stopped = stopped;
        }

        public boolean getStopped() {
            return this.stopped;
        }

        @Override
        public void run() {
            while (!isInterrupted() && !this.stopped) {
                try {
                    logger.info("WAVE-SCAN: is working - {}", this.getName());
                    if (null != refreshFileClientAssociation()) {
                        downloadFile();
                        try {
                            this.info.delFiles(Calendar.DATE, Config.getFileSaveDayNum() - 3);
                        } catch (Exception e0) {
                            logger.error("WAVE-DELETE: ", e0);
                        }
                    }
                } catch (Exception e) {
                    logger.error("WAVE-DOWNLOAD: ", e);
                }

                try {
                    Thread.sleep(Config.getFilePeriod() * 1000);
                } catch (Exception e) {
                }
            }

            if (fileClientAssociation != null) {
                fileClientAssociation.close();
            }
        }

        private List<String> downloadedFiles = new ArrayList<>();

        /**
         * 递归查找文件
         *
         * @param filePath      图谱文件获取路径
         * @param localFilePath 本地缓存图谱文件路径
         * @param files         图谱文件数据保存list
         * @throws Exception
         */
        private void listFile(String filePath, String localFilePath,
            List<Map<String, String>> files) throws Exception {
            List<FileInformation> fileInformation;
            log.debug("WAVE-STATUS-fileClientAssociation {}, {}", fileClientAssociation.isOpen(),
                filePath);
            try {
                fileInformation = fileClientAssociation.getFileDirectory(filePath);
            } catch (Exception e) {
                logger.error("getFileDirectory: ", e);
                return;
            }
            log.debug("WAVE-LIST: {}, {}", filePath, fileInformation.size());
            for (FileInformation infomation : fileInformation) {
                String fileName = infomation.getFilename();

                if (fileName.endsWith("./") || fileName.endsWith(".\\")
                    || fileName.endsWith(".")) {
                    continue;
                }

                String downloadFilePath = "";
                switch (Config.getWavfileDownloadMethod()) {
                    case 1: {
                        downloadFilePath = File.separator.concat(fileName);
                    }
                    break;
                    case 2: {
                        downloadFilePath = File.separator.concat(filePath).concat(fileName);
                    }
                    break;
                    case 3: {
                        downloadFilePath = filePath.concat(fileName);
                    }
                    break;
                }

                if (fileName.endsWith("/") || fileName.endsWith("\\")) {
                    listFile(downloadFilePath, localFilePath, files);
                    continue;
                }

                if (infomation.getLastModified() == null) {
                    log.debug("WAVE-INVALID-MODIFY: {}", downloadFilePath);
                    continue;
                }

                long fileModifiedMills = infomation.getLastModified().getTimeInMillis();
                //当非首次下载时，判断文件的最后更新时间是否早于上次下载时间，如果早于上次下载时间，则不进行二次下载
                if (info.getDownloadDate() != null) {
                    if (fileModifiedMills - info.getDownloadDate().getTime() <= 0) {
                        log.debug("WAVE-GET-EXIST: {}", downloadFilePath);
                        continue;
                    }
                }

                String lastFileName = fileName.substring(fileName.lastIndexOf(File.separator) + 1);
                File file = new File(localFilePath, lastFileName);
                Map<String, String> map = new HashMap<>();
                map.put("name", lastFileName);
                map.put("timestamp", timestampSDF.format(new Date(fileModifiedMills)));
                log.debug("WAVE-FILE: {}", downloadFilePath);
                FileOutputStream fos = new FileOutputStream(file);
                try {
                    fileClientAssociation.getFile(downloadFilePath, new PdmGetFileListener(fos, map, files));
                    if (info.getLastDownloadMills() < fileModifiedMills) {
                        info.updateLastDownloadMills(fileModifiedMills);
                    }
                    this.downloadedFiles.add(downloadFilePath);
                } catch (Exception e) {
                    logger.error("WAVE-FILE: {}, ", downloadFilePath, e);
                }
                try {
                    fos.close();
                } catch (Exception e) {
                }
            }
        }

        private void downloadFile() {
            Date newDate = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
            String newDateStr = sdf.format(newDate);
            File localFile = new File(info.getBaseDir(), newDateStr);
            localFile.mkdirs();
            info.updateLastDownloadMills(info.getDownloadDate().getTime());
            List<Map<String, String>> files = new ArrayList<>();
            try {
                this.downloadedFiles.clear();
                listFile(deviceConfig.getFileRootPath(), localFile.getAbsolutePath(), files);
                logger.info("DOWNLOAD-COUNT: {}", this.downloadedFiles.size());
            } catch (Exception e) {
                log.debug("WAVE-LIST: {} {} {} {}",
                    this.getName(), deviceConfig.getDeviceName(), deviceConfig.getFileRootPath(), e.getMessage());
                logger.error("设备ip:" + deviceConfig.getHost() + "下载文件异常", e);
            }
            this.downloadedFiles.clear();
            if (files.size() == 0) {
                logger.info("ip:" + deviceConfig.getHost() + "没有新的文件需要下载");
                localFile.delete();
                return;
            }
            info.setFiles(files);
            info.updateDownloadDate(new Date(info.getLastDownloadMills()));
            logger.info("WAVE-UPDATE: {}, {}, {}, {}",
                this.getName(), deviceConfig.getHost(), files.size(),
                info.getLastDownloadMills());
        }
    }

    public class PdmGetFileListener implements GetFileListener {
        private final List<byte[]> byteArrayList;
        private final FileOutputStream fos;
        private final Map<String, String> map;
        private final List<Map<String, String>> files;

        public PdmGetFileListener(FileOutputStream fos, Map<String, String> map, List<Map<String, String>> files) {
            this.byteArrayList = new ArrayList<>();
            this.fos = fos;
            this.map = map;
            this.files = files;
        }

        @Override
        public boolean dataReceived(byte[] fileData, boolean moreFollows) {
            try {
                fos.write(fileData);
                // 将得到的字节数组添加到字节数组List集合中
                byteArrayList.add(fileData);
            } catch (Exception ex) {
            }

            if (!moreFollows) {
                // 获取文件名称
                String fileName = map.get("name");
                try {
                    fos.flush();
                    // 获得字节数组List集合中的多个数组合并得到的一个数组
                    byte[] allFileData = ByteUtil.mergingByteArrays(byteArrayList);
                    // 判断文件名结尾是否是prpd、prps或者fullprpd，如果是，说明当前文件是单图谱信息文件，不需要进行拆分
                    if (fileName.endsWith("prpd") || fileName.endsWith("prps")
                        || fileName.endsWith("fullprpd") || Config.getLocalWaveFileVersion().equals("1")) {
                        map.put("value", ReportUtil.bytesToBase64(allFileData));
                        files.add(map);
                        if (Config.localLogDebug) {
                            logger.info("WAVE-FILE-1: {}", fileName);
                        }
                    } else {
                        // 如果是多图谱文件，则需要进行拆分，调用WavePackParser.parse()方法，将多图谱文件解析成多个单图谱文件
                        WavePackParser.parse(map, files, allFileData);
                        if (Config.localLogDebug) {
                            logger.info("WAVE-FILE-3: {}", fileName);
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    logger.error("WAVE-FILE-ERROR: {}, {}", fileName, ex.getMessage());
                }
            }
            return moreFollows;
        }
    }

//    public static class GIThread extends Thread {
//
//        private DataReportAcceptor dataAcceptor;
//        private boolean stopped = false;
//
//        public GIThread(DataReportAcceptor dataAcceptor) {
//            this.dataAcceptor = dataAcceptor;
//        }
//
//        public void setStopped(boolean stopped) {
//            this.stopped = stopped;
//        }
//
//        public boolean getStopped() {
//            return this.stopped;
//        }
//
//        @Override
//        public void run() {
//            logger.info("GI thread started: {}, {}, {}, {}", dataAcceptor.deviceConfig.getDeviceName(),
//                dataAcceptor.deviceConfig.getHost(), dataAcceptor.serverModel != null,
//                dataAcceptor.clientAssociation != null);
//            while (!isInterrupted() && dataAcceptor.serverModel != null
//                && !dataAcceptor.shutdownFlag && !this.stopped) {
//                if (null == dataAcceptor.refreshClientAssociation()) {
//                    break;
//                }
//                try {
//                    Thread.sleep(Config.getGiInterval());
//                } catch (Exception e1) {
//                }
//                for (Urcb rcb : dataAcceptor.getEnabledUrcbs()) {
//                    if (dataAcceptor.serverModel == null || dataAcceptor.clientAssociation == null) {
//                        break;
//                    }
//                    try {
//                        dataAcceptor.getClientAssociation().startGi(rcb);
//                    } catch (Exception e) {
//                        logger.error("Sending GI: {}, {}, {}, {}",
//                            this.getName(), dataAcceptor.deviceConfig.getDeviceName(),
//                            dataAcceptor.deviceConfig.getHost(), e.getMessage());
//                    }
//                }
//            }
//            logger.info("GI thread stopped: {}, {}, {}, {}, {}",
//                this.getName(), dataAcceptor.deviceConfig.getDeviceName(),
//                dataAcceptor.deviceConfig.getHost(), dataAcceptor.serverModel != null,
//                dataAcceptor.clientAssociation != null);
//        }
//    }

    @Data
    public static class SensorUpdate {
        String valMd5;
        long lastUpdate;
    }

    private Map<String, SensorUpdate> sensorUpdateMap = new HashMap<>();

    public boolean getShutdownFlag() {
        return this.shutdownFlag;
    }
}
