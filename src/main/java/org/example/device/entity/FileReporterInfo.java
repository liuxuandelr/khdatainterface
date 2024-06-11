package org.example.device.entity;

import cn.hutool.core.io.FileUtil;
import org.example.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class FileReporterInfo {
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
    private static final Logger logger = LoggerFactory.getLogger(FileReporterInfo.class);
    public static final String DOWNLOAD_DATE_FILE = ".download";
    private String baseDir;
    private Date sendDate;
    private Date downloadDate;
    private long lastDownloadMills;
    private String dateFilePath;
    private ConcurrentLinkedQueue<Map<String, String>> files = new ConcurrentLinkedQueue<>();

    public synchronized String getBaseDir() {
        return this.baseDir;
    }

    public synchronized void setBaseDir(String baseDir) {
        this.baseDir = baseDir;
        (new File(baseDir)).mkdirs();
    }

    public synchronized Date getSendDate() {
        return this.sendDate;
    }

    public synchronized void setSendDate(Date sendDate) {
        this.sendDate = sendDate;
    }

    public synchronized Date getDownloadDate() {
        return this.downloadDate;
    }

    public synchronized void setDownloadDate(Date downloadDate) {
        this.downloadDate = downloadDate;
    }

    public void initDownloadDate() {
        this.dateFilePath = getBaseDir().concat(File.separator).concat(DOWNLOAD_DATE_FILE);
        Path path = Paths.get(dateFilePath);
        if (Files.exists(path) && Files.isReadable(path)) {
            try {
                List<String> lines = Files.readAllLines(path);
                if (lines != null && lines.size() > 0) {
                    this.downloadDate = sdf.parse(lines.get(0));
                }
            } catch (Exception e) {
                logger.error("WAVE-DOWN-DATE: {}, {}", dateFilePath, e.getMessage());
            }
        }

        if (this.downloadDate == null) {
            Date now = new Date();
            // TODO
            this.downloadDate = new Date(now.getTime() - 3600000L * 24 * 365);
//            this.downloadDate = new Date(now.getTime() - Duration.ofHours(24L).toMillis());
        }

        this.lastDownloadMills = this.downloadDate.getTime();

        this.initFiles();
    }



    public void updateDownloadDate(Date date) {
        this.downloadDate = date;
        List<String> lines = new ArrayList<>();
        try {
            lines.add(this.sdf.format(this.downloadDate));
            Files.write(Paths.get(dateFilePath), lines);
        } catch (IOException e) {
            logger.error("WAVE-DOWN-DATE-UPD: {}, {}", this.downloadDate, e.getMessage());
        }
    }

    public void updateLastDownloadMills(long lastDownloadMills) {
        this.lastDownloadMills = lastDownloadMills;
    }

    public long getLastDownloadMills() {
        return this.lastDownloadMills;
    }

    public boolean getSended(String filePath) {
        filePath = getBaseDir().concat(File.separator).concat(filePath).concat(File.separator).concat("_success");
        File file = new File(filePath);
        return file.exists();
    }

    public synchronized void setSended() {
        try {
            delFiles();
            String filePath = getBaseDir().concat(File.separator).concat(sdf.format(this.downloadDate)).concat(File.separator).concat("_success");
            File file = new File(filePath);
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (Exception ignored) {
        }
    }

    // 删除历史图谱
    public void delFiles() {
        delFiles(Calendar.DATE, Config.getFileSaveDayNum());
    }

    // 删除历史图谱
    public void delFiles(Integer calendar, Integer days) {
        Calendar c = Calendar.getInstance();
        c.setTime(this.downloadDate);
        if (days != null && days < 0) {
            c.add(calendar, days);
        }
        String dateStr = sdf.format(c.getTime());
        String[] fileNames = (new File(getBaseDir())).list();
        if (fileNames == null) {
            return;
        }
        for (String fileName : fileNames) {
            if (fileName.startsWith(".")) {
                continue;
            }
            if (Double.parseDouble(fileName) - Double.parseDouble(dateStr) <= 0.0D) {
                FileUtil.del(this.baseDir+File.separator+fileName);

            }
        }
    }

    public synchronized List<Map<String, String>> getFiles() {
        List<Map<String, String>> result = new ArrayList<>();
        while (!files.isEmpty()) {
            result.add(files.poll());
        }
        return result;
    }

    public boolean filesIsEmpty() {
        return this.files.isEmpty();
    }

    public synchronized void setFiles(List<Map<String, String>> newFiles) {
        for (Map<String, String> fileItem : newFiles) {
            this.files.add(fileItem);
        }
    }

    private void initFiles() {
        this.files = new ConcurrentLinkedQueue<>();
        /*
        if (this.downloadDate == null)
            return;
        File fi = new File(this.baseDir, sdf.format(this.downloadDate));
        if (getSended(fi.getName()))
            return;
        File[] _fis = (new File(getBaseDir(), fi.getName())).listFiles();
        if (_fis != null && _fis.length != 0) {
            for (File _fi : _fis) {
                Map<String, String> map = new HashMap<>();
                map.put("name", _fi.getName());
                StringBuilder sb = new StringBuilder();
                try (FileInputStream in = new FileInputStream(_fi)) {
                    byte[] buff = new byte[1024];
                    int len;
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    while ((len = in.read(buff)) != -1) {
                        bos.write(buff, 0, len);
                    }
                    bos.flush();
                    sb.append(ReportUtil.bytesToBase64(bos.toByteArray()));
                    map.put("value", sb.toString());
                    this.files.add(map);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
         */
    }
}
