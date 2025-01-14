package org.example.task;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.example.client.SampleDataBuilder;
import org.example.config.Config;
import org.example.config.DataType;
import org.example.controller.RangingRecordController;
import org.example.device.report.DataReportAcceptor;
import org.example.pojo.*;
import org.example.service.impl.EventsServiceImpl;
import org.example.service.impl.FaultdatasServiceImpl;
import org.example.utils.ByteUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class Task10Time {
    @Resource
    public FaultdatasServiceImpl faultdatasService;
    @Autowired
    public EventsServiceImpl eventsService;
    @Resource
    public RangingRecordController rangingRecordController;
    @Resource
    public DataReportAcceptor dataReportAcceptor;

    private int getNums = 1;

    @Scheduled(cron = "0 0/10 * * * ?")
    @PostConstruct
    public void doTesk() throws ParseException {
        log.info("获取数据任务开始第{}轮--{}", getNums, new Date(System.currentTimeMillis()));
        EventSystate sysdate =eventsService.intgetMaxTime();
        if (sysdate==null){
            log.info("无最新故障测距信息");
        }
        Date date = rangingRecordController.intGetMaxTime();
        FaultdatasEntity topTime = faultdatasService.getTopTime();
        String faultTime = topTime.getFaultTime();
        SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date1 = ft.parse(faultTime);
        log.info("读取的本地时间:{}，读取的最新时间:{}", date, date1);
        ArrayList<RangingRecordEntity> recordEntities = new ArrayList<>();
        ArrayList<SensorSampleData> sampleData = new ArrayList<>();
        if (date.compareTo(date1) == 0){
            log.info("没有新的时间数据，发送最新的宜昌端1线和2线故障数据");
            recordEntities = rangingRecordController.notDateTime(date1,true);
        } else if (date.compareTo(date1) < 0){
            recordEntities = rangingRecordController.inMaxTimeGetListFault(true);
        }else if (date.compareTo(date1) > 0){
            log.info("文件内时间戳有误，发送最新的宜昌端1线和2线故障数据");
            recordEntities = rangingRecordController.notDateTime(date1, true);
        }
        //TODO        获取日志表中的数据，Fault和Event同时间数量大约2：1,为保证避免干扰日志扩大获取范围,可考虑取出对比而非查询
        Map<String, EventsEntity> timeEventsMap = eventsService.allEventsList(recordEntities.size() * 2);
        Map<String, EventsEntity> stringEventsEntityMap = new HashMap<>();
        //键转换为String格式日期
        timeEventsMap.forEach((a, b) -> {
            try {
                stringEventsEntityMap.put(ft.format(ft.parse(a)), b);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        });
        //筛选集合中时间小于文本记录时间的数据
        Iterator<Map.Entry<String, EventsEntity>> iterator = stringEventsEntityMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, EventsEntity> entry = iterator.next();
            if (date.compareTo(ft.parse(entry.getKey())) > 0) {
                iterator.remove();
            }
        }
        //        从Json文件中取得四条线路的sensorId和其他表对应关系id
        HashMap<String, String[]> hashMap = new HashMap<>();
        recordEntities.stream().forEach(a -> {
            List<SensorEntity> sensorEntities = Config.sensorEntityList();
            sensorEntities.stream().forEach(b -> {
                if (a.getLineName().equals(b.getLinesId().equals("2") ? "盘宜Ⅰ线" : "盘宜Ⅱ线")) {
                    a.setSensorId(b.getSensorId());
                    a.setLineLen("229.684");
                } else {
                    a.setSensorId(b.getSensorId());
                    a.setLineLen("231.084");
                }
            });
//            截断日期查询查询日志Map的键，判断其产生的告警类型和告警序号
            String t = a.getFlWaveSelfT().toString().replace("T", " ");
            EventsEntity eventsEntity = stringEventsEntityMap.get(t);
            if (eventsEntity == null || StringUtils.isBlank(eventsEntity.getType())) {
                a.setAlm1(false);
                a.setAlm2(false);
                a.setAlm3(false);
                a.setAlm4(false);
                a.setFltNum(0);
            } else {
                switch (eventsEntity.getType()) {
                    case "1":
                        a.setAlm1(true);
                        a.setFltNum(1);
                    case "2":
                        a.setAlm2(true);
                        a.setFltNum(2);
                    case "3":
                        a.setAlm3(true);
                        a.setFltNum(3);
                        break;
                    case "5":
                        a.setAlm1(false);
                        a.setAlm2(false);
                        a.setAlm3(false);
                        a.setAlm4(false);
                        a.setFltNum(0);
                        break;
                }
            }
            sampleData.add(SampleDataBuilder.buildSampleData(a));

            //获取图谱与对应信息，并且封装到Map集合中准备发送
            hashMap.put(a.getFaultId(), new String[] {
                a.getSensorId(),
                a.getSubName(),
                a.getPeerSubName(),
                a.getLineName(),
                a.getFlWaveSelfT(),
                ByteUtil.toHexStringTrim(a.getFileData())
            });
        });
        //发数据
        if (sendData(sampleData, DataType.POINT_DATA.toString()) == 0) {
            rangingRecordController.outGetMaxTime();
            log.info("已覆盖本地时间：{}，最新时间：{}", date, date1);
        }
        //下载图谱
        //TODO 使用61850服务下载图谱
//        DownBlobFile(hashMap);
        //发图谱
        List<SensorSampleData> sampleFile = new ArrayList<>();
        hashMap.forEach((a, b) -> {
            List<FileData> fileData = new ArrayList<>();
            FileData fileData1 = new FileData(
                b[1] + "-" + b[2] + "-" + b[3] + "-" + b[4].replaceAll(":","").replaceAll("-","").replaceAll(" ","") + ".dat",
                b[5]);
            fileData.add(fileData1);
            SensorSampleData sampleDataFile = new SensorSampleData();
            try {
                sampleDataFile = SampleDataBuilder.buildSampleData(
                    b[0],
                    b[1] + "-" + b[2], b[3],
                    ft.parse(b[4]), fileData);
                sampleFile.add(sampleDataFile);
                log.info("已打包图谱-FILE：{}", sampleDataFile.toString().substring(0, 500));
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        });
        if (sendData(sampleFile, DataType.POINT_DATA.toString()) == 0) {
            log.info("图谱打包完成");
        }
        log.info("第{}轮获取数据任务结束--{}", getNums, new Date(System.currentTimeMillis()));
        getNums++;
    }

    //    下载文件到本地
    public void DownBlobFile(HashMap<String, String[]> hashMap) {
        String basePath = System.getProperty("user.dir");
//        File path = new File(basePath + File.separator + "blob");
        File path = new File(basePath + File.separator + "files");
        if (!path.exists()) {
            path.mkdir();
        }
        hashMap.forEach((a, b) -> {
            String pathFile = path + File.separator + b[1] + "-" + b[2] + "-" + b[3] + "-"
                + b[4].replaceAll(":", "") + ".dat";
            File file = new File(pathFile);
            try {
                if (!file.exists())
                    file.createNewFile();
                BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file));
                out.write(b[5].getBytes());
                out.close();
            } catch (IOException io) {
                io.printStackTrace();
            }
        });
    }

    //发送程序
    public int sendData(List<SensorSampleData> samples, String dataType) {
        try {
            String dataBody = JSONObject.toJSONString(samples);
            log.info("KeHui-DATA: {}", StringUtils.substring(dataBody, 0, 500));
            if (samples.size() > 0) {
                JSONArray jsonArrayData = JSONArray.parseArray(dataBody);
                Config.getSocketSender().addData(jsonArrayData);
                samples = new ArrayList<>();
                System.gc();
            }
        } catch (Exception e) {
            log.error("KeHui submit data sender: ", e);
        }
        return samples.size();
    }

}
