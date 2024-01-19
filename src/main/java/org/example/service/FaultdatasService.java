package org.example.service;

import org.example.pojo.FaultdatasEntity;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.stereotype.Component;

import java.lang.reflect.Array;
import java.sql.Blob;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author ruiliu
 * @since 2024-01-15 12:48:18
 */
@Component
public interface FaultdatasService extends IService<FaultdatasEntity> {

    public FaultdatasEntity getTopTime();

    public ArrayList<FaultdatasEntity> getTopNum2Time(Date data);

    //    下载blob文件
    public String getBlobFileById(String faultId);
}
