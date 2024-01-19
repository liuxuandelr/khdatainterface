package org.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.example.dao.FaultdatasMapper;
import org.example.pojo.FaultdatasEntity;
import org.example.service.FaultdatasService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author ruiliu
 * @since 2024-01-15 12:48:18
 */
@Service
@Slf4j
public class FaultdatasServiceImpl extends ServiceImpl<FaultdatasMapper, FaultdatasEntity> implements FaultdatasService {

    @Resource
    private FaultdatasMapper faultdatasMapper;

    //获得最先的数据时间准备下放到文件中
    @Override
    public FaultdatasEntity getTopTime() {
        FaultdatasEntity faultdatasEntity = new FaultdatasEntity();
        QueryWrapper<FaultdatasEntity> qw1 = new QueryWrapper<>(faultdatasEntity);
        List<FaultdatasEntity> list = list(qw1.orderByDesc("faultTime"));
        // TODO 查所有占用资源可优化
        return list.get(0);
    }

    //根据文件中的时间找判断新的数据存在
    @Override
    public ArrayList<FaultdatasEntity> getTopNum2Time(Date date) {
        FaultdatasEntity faultdatasEntity = new FaultdatasEntity();
        QueryWrapper<FaultdatasEntity> qw1 = new QueryWrapper<>(faultdatasEntity);
        List<FaultdatasEntity> list = list(qw1.orderByDesc("faultTime"));
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        ArrayList<FaultdatasEntity> arrayList = new ArrayList<>();
        list.stream().forEach(a -> {
            try {
                Date date1 = fmt.parse(a.getFaultTime());
                if (date1.compareTo(date) > 0) {
                    arrayList.add(a);
                }

            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        });
        return arrayList;
    }

    @Override
    public String getBlobFileById(String faultId) {
        String blob = faultdatasMapper.selectBlob(faultId);
        return blob;
    }

}
