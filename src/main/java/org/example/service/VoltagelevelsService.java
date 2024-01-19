package org.example.service;

import org.example.pojo.VoltagelevelsEntity;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author ruiliu
 * @since 2024-01-15 12:48:18
 */
public interface VoltagelevelsService extends IService<VoltagelevelsEntity> {

    public VoltagelevelsEntity voltageGetById(String id);
}
