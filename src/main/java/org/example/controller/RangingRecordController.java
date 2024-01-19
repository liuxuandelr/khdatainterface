package org.example.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.example.pojo.FaultdatasEntity;
import org.example.pojo.LinesEntity;
import org.example.pojo.RangingRecordEntity;
import org.example.pojo.VoltagelevelsEntity;
import org.example.service.impl.FaultdatasServiceImpl;
import org.example.service.impl.LinesServiceImpl;
import org.example.service.impl.VoltagelevelsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author ruiliu
 * @since 2024-01-15 16:53:46
 */
@Slf4j
@RestController
@RequestMapping("/ranging-record-entity")
public class RangingRecordController {

    @Autowired
    public FaultdatasServiceImpl faultdatasService;

    @Autowired
    public LinesServiceImpl linesService;

    @Autowired
    public VoltagelevelsServiceImpl voltagelevelsService;

    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public void outGetMaxTime() {
        FaultdatasEntity topTime = faultdatasService.getTopTime();
        String faultTime = topTime.getFaultTime();
        String basePath = System.getProperty("user.dir");
        String path = basePath + File.separator + "faultTime.txt";
        File file = new File(path);
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file));
            byte[] bb = faultTime.getBytes();
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
    }

    //  从数据库内获取最新的数据时间
    public Date intGetMaxTime() {
        String basePath = System.getProperty("user.dir");
        String path = basePath + File.separator + "faultTime.txt";
        Date date = null;
        try {
            FileInputStream input = new FileInputStream(path);
            byte[] bytes2 = new byte[1024];
            int len = input.read(bytes2);
            String time = new String(bytes2, 0, len);
            SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            date = ft.parse(time);
            input.close();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return date;
    }

    //    查询所有超过本地时间的数据
    public ArrayList<RangingRecordEntity> inMaxTimeGetListFault() {
        Date date = this.intGetMaxTime();
        log.info("data {}", date);
        ArrayList<FaultdatasEntity> topNum2Time = faultdatasService.getTopNum2Time(date);
        ArrayList<RangingRecordEntity> recordEntities = new ArrayList<>();
        topNum2Time.stream().forEach(a -> {
            RangingRecordEntity rangingRecord = new RangingRecordEntity();
            rangingRecord.setFlWavePeerT(LocalDateTime.parse(a.getFaultTime(), fmt));
            rangingRecord.setFlWaveSelfT(LocalDateTime.parse(a.getFaultTime(), fmt));
            rangingRecord.setFaultA(a.getAmplitude() != null ? a.getAmplitude().toString() : "null");
            rangingRecord.setFaultId(a.getId());
            rangingRecord.setFaultTimeUs(String.valueOf(a.getUs()));
            LinesEntity linesEntity = linesService.lineIdGetLine(a.getLineId());
            rangingRecord.setLineName(linesEntity.getName());
            rangingRecord.setLineLen(String.valueOf(linesEntity.getLength()));
            if (a.getDeviceId() == 1) {
                rangingRecord.setSubName("宜昌换流站");
                rangingRecord.setPeerSubName("九盘变");
            } else if (a.getDeviceId() == 3) {
                rangingRecord.setSubName("九盘变");
                rangingRecord.setPeerSubName("宜昌换流站");
            }
            VoltagelevelsEntity byId = voltagelevelsService.getById(linesEntity.getVoltagelevelId());
            rangingRecord.setFaultV(String.valueOf(byId.getName()));
            rangingRecord.setWaveSpd(String.valueOf(linesEntity.getSpeed()));
            try {
                rangingRecord.setFileData(a.getData());
                log.info("BLOB-LENGTH: {}", a.getData().length);
                recordEntities.add(rangingRecord);
            } catch (Exception e) {
                log.error("FETCH BLOB: ", e);
            }
        });
        return recordEntities;
    }

    //根据时间查询两条线路最新的数据
    public ArrayList<RangingRecordEntity> notDateTime(Date date1) {
        List<LinesEntity> list = linesService.list();
        ArrayList<RangingRecordEntity> recordEntities = new ArrayList<>();
        for (LinesEntity linesEntity : list) {
            QueryWrapper<FaultdatasEntity> qw = new QueryWrapper<>();
            FaultdatasEntity one = faultdatasService.getOne(qw.eq("faultTime",
                ft.format(date1)).eq("lineId", linesEntity.getId()));
            RangingRecordEntity rangingRecord = new RangingRecordEntity();
            rangingRecord.setFlWavePeerT(LocalDateTime.parse(one.getFaultTime(), fmt));
            rangingRecord.setFlWaveSelfT(LocalDateTime.parse(one.getFaultTime(), fmt));
            rangingRecord.setFaultA(one.getAmplitude() != null ? one.getAmplitude().toString() : "null");
            rangingRecord.setFaultId(one.getId());
            rangingRecord.setFaultTimeUs(String.valueOf(one.getUs()));
            rangingRecord.setLineName(linesEntity.getName());
            rangingRecord.setLineLen(String.valueOf(linesEntity.getLength()));
            if (one.getDeviceId() == 1) {
                rangingRecord.setSubName("宜昌换流站");
                rangingRecord.setPeerSubName("九盘变");
            } else if (one.getDeviceId() == 3) {
                rangingRecord.setPeerSubName("九盘变");
                rangingRecord.setPeerSubName("宜昌换流站");
            }
            VoltagelevelsEntity byId = voltagelevelsService.getById(linesEntity.getVoltagelevelId());
            rangingRecord.setFaultV(String.valueOf(byId.getName()));
            rangingRecord.setWaveSpd(String.valueOf(linesEntity.getSpeed()));
            try {
                rangingRecord.setFileData(one.getData());
                log.info("BLOB-LENGTH: {}", one.getData().length);
                recordEntities.add(rangingRecord);
            } catch (Exception e) {
                log.error("FETCH BLOB: ", e);
            }
        }
        return recordEntities;
    }
}
