package org.example.service.impl;

import org.example.pojo.VoltagelevelsEntity;
import org.example.dao.VoltagelevelsMapper;
import org.example.service.VoltagelevelsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author ruiliu
 * @since 2024-01-15 12:48:18
 */
@Service
public class VoltagelevelsServiceImpl extends ServiceImpl<VoltagelevelsMapper, VoltagelevelsEntity> implements VoltagelevelsService {

    @Override
    public VoltagelevelsEntity voltageGetById(String id) {
        VoltagelevelsEntity byId = getById(id);
        return byId;
    }
}
