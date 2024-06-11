package org.example.utils;

import org.apache.commons.codec.binary.Base64;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

public class ReportUtil {

    /***
     * 对指定的字符串进行MD5加密
     */
    public static String encrypByMD5(String originString) {
        try {
            // 创建具有MD5算法的信息摘要
            MessageDigest md = MessageDigest.getInstance("MD5");
            // 使用指定的字节数组对摘要进行最后更新，然后完成摘要计算
            byte[] bytes = md.digest(originString.getBytes());
            // 将得到的字节数组变成字符串返回
            String s = byteArrayToHex(bytes);
            return s.toUpperCase();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 返回字节数组经过Base64编码之后的字符串
     *
     * @param bytes
     * @return
     */
    public static String bytesToBase64(byte[] bytes) {
        StringBuffer sbf = new StringBuffer();
        sbf.append(Base64.encodeBase64String(bytes));
        return sbf.toString();
    }

    private static String byteArrayToHex(byte[] b) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < b.length; i++) {
            sb.append(byteToHex(b[i]));
        }
        return sb.toString();
    }

    public static String byteToHex(byte b) {
        return String.format("%02x", new Integer(b & 0xff));
    }

    public static String createID() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }
}
