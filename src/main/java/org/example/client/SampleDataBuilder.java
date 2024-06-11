package org.example.client;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import org.example.config.AttrData;
import org.example.pojo.FileData;
import org.example.pojo.RangingRecordEntity;
import org.example.pojo.SensorSampleData;

public class SampleDataBuilder {
    public static final String TYPE_PROTECT = "027001";
    public static final String TYPE_FAULT = "027002";

    private static SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static SensorSampleData buildSampleData(String objectId, String objectName,
        List<RangingRecordEntity> valuePos) {
        SensorSampleData result = new SensorSampleData();
        result.setType(TYPE_PROTECT);
        result.setSensorid(objectId);
        result.setEquipmentid(result.getSensorid());
        result.setName(objectName);
        result.setTimestamp(SDF.format(valuePos.get(0).getFlWaveSelfT()));
        return result;
    }

    public static SensorSampleData buildSampleData(RangingRecordEntity objectPo) {
        Class<?> clazz = objectPo.getClass();
        SensorSampleData result = new SensorSampleData();
        result.setType(TYPE_PROTECT);
        result.setSensorid(objectPo.getSensorId());
        result.setEquipmentid(result.getSensorid());
        result.setName(objectPo.getSubName() + "-" + objectPo.getPeerSubName());
        result.setTimestamp(objectPo.getFlWaveSelfT());
        List<PropertyDescriptor> descriptors = null;
        HashMap<String, Object> map = new HashMap();
        try {
            descriptors = Arrays.stream(Introspector.getBeanInfo(clazz).getPropertyDescriptors()).filter(p -> {
                String name = p.getName();
                //过滤掉不需要修改的属性
                return !"class".equals(name) &&
//                    !"alm1".equals(name) &&
//                    !"alm2".equals(name) &&
//                    !"alm3".equals(name) &&
//                    !"alm4".equals(name) &&
                    !"id".equals(name) &&
                    !"faultId".equals(name) &&
                    !"createTime".equals(name);
            }).collect(Collectors.toList());
            for (PropertyDescriptor descriptor : descriptors) {
                Method readMethod = descriptor.getReadMethod();
                Object invoke = readMethod.invoke(objectPo);
                map.put(descriptor.getName(), invoke);
            }
        } catch (IntrospectionException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        List<AttrData> attrDataList = new ArrayList<>();
        map.forEach((a, b) -> {
            AttrData attrData = new AttrData();
            attrData.setName(a);
            if (b != null) {
                attrData.setValue(b.toString());
            } else {
                attrData.setValue("null");
            }
            attrDataList.add(attrData);
        });
        result.setAttrs(attrDataList);
        return result;
    }

    /**
     * 录波文件推送结构
     *
     * @param files 文件名称、文件内容
     * @return
     */
    public static SensorSampleData buildSampleData(String objectId, String objectName,
        String fileDesc, Date faultTime, List<FileData> files) {
        SensorSampleData result = new SensorSampleData();
        result.setType(TYPE_FAULT);
        result.setSensorid(objectId);
        result.setEquipmentid(result.getSensorid());
        result.setName(objectName + "#" + fileDesc);
        result.setTimestamp(SDF.format(faultTime));
        result.setFiles(files);
        return result;
    }

}
