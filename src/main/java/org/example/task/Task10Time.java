package org.example.task;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
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
import org.example.pojo.EventsEntity;
import org.example.pojo.FaultdatasEntity;
import org.example.pojo.FileData;
import org.example.pojo.RangingRecordEntity;
import org.example.pojo.SensorEntity;
import org.example.pojo.SensorSampleData;
import org.example.service.impl.EventsServiceImpl;
import org.example.service.impl.FaultdatasServiceImpl;
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

    private int getNums = 1;

    @Scheduled(cron = "0 0/10 * * * ?")
    @PostConstruct
    public void doTesk() throws ParseException {
        log.info("获取数据任务开始第{}轮--{}", getNums, new Date(System.currentTimeMillis()));
        Date date = rangingRecordController.intGetMaxTime();
        FaultdatasEntity topTime = faultdatasService.getTopTime();
        String faultTime = topTime.getFaultTime();
        SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date1 = ft.parse(faultTime);
        log.info("读取的本地时间:{}，读取的最新时间:{}", date, date1);
        ArrayList<RangingRecordEntity> recordEntities;
        ArrayList<SensorSampleData> sampleData = new ArrayList<>();
        if (date.compareTo(date1) >= 0) {
            log.info("没有新的时间数据，发送最新的宜昌1线和2线数据");
            recordEntities = rangingRecordController.notDateTime(date1);
        } else {
            recordEntities = rangingRecordController.inMaxTimeGetListFault();
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
                } else {
                    a.setSensorId(b.getSensorId());
                }
            });
//            截断日期查询查询日志Map的键，判断其产生的告警类型和告警序号
            String t = a.getFlWaveSelfT().toString().replace("T", " ");
            EventsEntity eventsEntity = stringEventsEntityMap.get(t);
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
            sampleData.add(SampleDataBuilder.buildSampleData(a));
            //TODO 数据长度不足500会报错，需要判断长度优化
            log.info("已打包数据-DATA：{}", SampleDataBuilder.buildSampleData(a).toString().substring(0, 500));
            //获取图谱与对应信息，并且封装到Map集合中准备发送
            hashMap.put(a.getFaultId(), new String[] {
                a.getSensorId(),
                a.getSubName(),
                a.getPeerSubName(),
                a.getLineName(),
                String.valueOf(a.getFlWaveSelfT()),
                faultdatasService.getBlobFileById(a.getFaultId())
            });
        });
        //发数据
        if (sendData(sampleData, DataType.POINT_DATA.toString()) == 0) {
            rangingRecordController.outGetMaxTime();
            log.info("已覆盖本地时间：{}，最新时间：{}", date, date1);
        }
//        下载图谱
        DownBlobFile(hashMap);
        //发图谱
        List<SensorSampleData> sampleFile = new ArrayList<>();
        hashMap.forEach((a, b) -> {
            List<FileData> fileData = new ArrayList<>();
            FileData fileData1 = new FileData(b[1] + "-" + b[2] + "-" + b[3] + "-" + b[4].replaceAll(":", "") + ".dat", Base64.getEncoder().encodeToString(b[5].getBytes(StandardCharsets.UTF_8)));
            fileData.add(fileData1);
            SensorSampleData sampleDataFile = new SensorSampleData();
            try {
                sampleDataFile = SampleDataBuilder.buildSampleData(
                    b[0],
                    b[1] + "-" + b[2],
                    b[1] + "-" + b[2] + b[3],
                    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(b[4].replaceAll("T", " ")), fileData);
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
        File path = new File(basePath + File.separator + "blob");
        if (!path.exists()) {
            path.mkdir();
        }
        hashMap.forEach((a, b) -> {
            String pathFile = path + File.separator + b[1] + "-" + b[2] + "-" + b[3] + "-" + b[4].replaceAll(":", "") + ".dat";
            File file = new File(pathFile);
            try {
                if (!file.exists())
                    file.createNewFile();
                BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file));
                byte[] bb = b[5].getBytes();
                int offset = 0;// 每次读取到的字节数组的长度
                int chunkSize = 8192;
                while (offset < bb.length) {
                    int length = Math.min(chunkSize, bb.length - offset);
                    out.write(bb, 0, length);// 写入到输出流
                    offset += length;
                }
                out.flush();
                out.close();// 关闭流
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
