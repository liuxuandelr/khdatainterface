package org.example.dao;

import org.example.pojo.FaultdatasEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author ruiliu
 * @since 2024-01-15 12:48:18
 */
@Mapper
@Component
public interface FaultdatasMapper extends BaseMapper<FaultdatasEntity> {

    public String selectBlob(String faultId);
}
