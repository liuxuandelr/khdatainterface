package org.example.device.report.wavefile;

import org.apache.commons.codec.binary.Base64;
import org.example.config.Config;
import org.example.utils.ReportUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WavePackParser {
    private static final Logger logger = LoggerFactory.getLogger(WavePackParser.class);

    /**
     * 序号  检测类型	 图谱类型          编码   文件后缀
     * 1     特高频局放   特高频多图谱      0x20
     * 2		         特高频PRPD图      0x21   .prpd
     * 3		         特高频PRPS图      0x22   .prps
     * 4		         特高频峰值统计图   0x23   .stat
     *
     * @param file     传入的文件相关信息，以Map集合存储，key值包括（name：文件名称）
     * @param files    解析传入的文件内容后，存储得到的多个图谱信息集合
     *                 （在Map集合中，key：FileName，value：AtlasInfo）
     * @param fileData 图谱数据
     * @return 返回文件中解析出了几个图谱文件
     */
    public static int parse(Map<String, String> file, List<Map<String, String>> files, byte[] fileData) {

        // 获取解析文件名称
        String originalFileName = file.get("name");
        // 创建StringBuilder变量，用以存储修改更新后的解析文件名称
        StringBuilder fileName;

        // 以下数值设置计算均按照《特高压GIS设备特高频局放图谱接入规范0909 》图谱解析文档来计算
        int atlasStartIndex = 512;  // 第一个图谱文件开始下标
        /*
         * 创建ByteBuffer对象，解析文件所需的关键数据，并提取文件中的图谱数据
         * 设置当前下标开始位置（position）为512，0-511部分为多图谱文件头部内容，不需要
         */
        ByteBuffer byteBufferInfo = (ByteBuffer) ByteBuffer.wrap(fileData)
                .order(ByteOrder.LITTLE_ENDIAN).position(atlasStartIndex);

        int fileLength = byteBufferInfo.getInt(0);  // 文件长度
        short atlasNum = byteBufferInfo.getShort(286);  // 图谱数量
        int count = 0;
        // 循环取出多个图谱数据
        while (atlasStartIndex < fileLength - 36) {
            // 创建Map集合，存储单个图谱数据信息
            Map<String, String> atlasMap = new HashMap<>();
            // 图谱类型编码
            byte atlasType = byteBufferInfo.get(atlasStartIndex);
            // 为提取出的图谱信息配置文件名
            fileName = null;
            switch (atlasType) {
                case 0x21:
                    // PRPD图，文件后缀：.prpd
                    fileName = new StringBuilder(originalFileName).append(".prpd");
                    break;
                case 0x22:
                    // PRPS图，文件后缀：.prps
                    fileName = new StringBuilder(originalFileName).append(".prps");
                    break;
                case 0x23:
                    // 特高频峰值统计图，文件后缀：.stat
                    fileName = new StringBuilder(originalFileName).append(".stat");
                    break;
            }
            if (fileName == null) {
                logger.error("WAVE-PARSE: {}, type: {} - {}", originalFileName, atlasStartIndex, atlasType);
                break;
            }
            // 图谱数据长度
            int atlasDataLength = byteBufferInfo.getInt(atlasStartIndex + 1);

            // 获取取图谱数据
            byte[] atlasData = new byte[atlasDataLength];
            byteBufferInfo.get(atlasData, 0, atlasDataLength);

            // 将单个图谱相关信息存入Map集合中
            String base64 = ReportUtil.bytesToBase64(atlasData);
            atlasMap.put("name", fileName.toString());
            atlasMap.put("value", base64);
            atlasMap.put("timestamp", file.get("timestamp"));

            if (Config.localLogDebug) {
                logger.info("WAVE-FILE-2: {}, {}", fileName, base64.length());
            }
            ++count;

            // 将存储单个图谱相关信息的Map集合添加到List集合中
            files.add(atlasMap);

            // 获取下一个图谱开始下标
            atlasStartIndex += atlasDataLength;
        }

        // 返回文件中解析出了几个图谱文件
        return count;
    }

    public static void main(String[] args) throws Exception {
        Map<String, String> map = new HashMap<>();
        map.put("name", "20230727114425-MerkPDEC3000_PDS187_20230727114424026.dat");
//        map.put("name", "20230727114425-MerkPDEC3000_PDS187_20230727114424026.dat");
        List<Map<String, String>> files = new ArrayList<>();
        DataInputStream dis = new DataInputStream(new FileInputStream(
//            new File("~/Downloads/GZAndianUF-9000_C001D0001S001_20221024151528977.dat")));
            "D:\\IDEAspace\\atlas_search\\doc\\潇湘站华电云通500kv图谱解析\\华电云通500kv局放\\JF5001_MONT04_SPDC22_32_20240124174549944.dat"));
//            "/Users/sunzhaoyu/Documents/20230727114425-MerkPDEC3000_PDS187_20230727114424026.dat"));
        Config.localLogDebug = true;
        byte[] buffer = new byte[dis.available()];
        int read = 0;
        while ((read = dis.read(buffer, 0, buffer.length)) != -1) {
            try {
                int parse = parse(map, files, buffer);
                for (Map<String, String> file : files) {
                    String name = file.get("name");
                    String value = file.get("value");
                    Files.write(Paths.get(name), Base64.decodeBase64(value));
                }
                System.out.println("解析出图谱文件个数：" + parse);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
