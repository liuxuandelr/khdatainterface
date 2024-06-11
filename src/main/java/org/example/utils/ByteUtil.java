package org.example.utils;

import java.util.List;

public class ByteUtil {

    public static byte hexToByte(String inHex) {
        return (byte) Integer.parseInt(inHex, 16);
    }

    public static byte[] hexToByteArray(String inHex) {
        int hexlen = inHex.length();
        byte[] result;
        if (hexlen % 2 == 1) {
            hexlen++;
            result = new byte[(hexlen / 2)];
            inHex = "0" + inHex;
        } else {
            result = new byte[(hexlen / 2)];
        }
        int j = 0;
        for (int i = 0; i < hexlen; i += 2) {
            result[j] = hexToByte(inHex.substring(i, i + 2));
            j++;
        }
        return result;
    }

    public static String toHexStringTrim(byte[] buffer) {
        return toHexStringTrim(buffer, 0, buffer.length);
    }

    public static String toHexStringTrim(byte[] buffer, int srcPos, int length) {
        StringBuilder builder = new StringBuilder();
        for (int id = srcPos; id < srcPos + length; ++id) {
            String hexStr = Integer.toHexString(buffer[id] & 0xFF);
            if (hexStr.length() < 2) {
                builder.append("0");
            }
            builder.append(hexStr);
        }
        return builder.toString();
    }


    public static byte[] mergingByteArrays(List<byte[]> values) {
        // 计算合并后数组长度
        int lengthByte = 0;
        for (byte[] value : values) {
            lengthByte += value.length;
        }
        // 将多个数组的数据合并到1个数组中
        byte[] allBytes = new byte[lengthByte];
        int countLength = 0;
        for (byte[] b : values) {
            System.arraycopy(b, 0, allBytes, countLength, b.length);
            countLength += b.length;
        }
        // 返回合并后的字节数组
        return allBytes;
    }
}
