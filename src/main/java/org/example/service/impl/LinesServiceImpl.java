package org.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.example.pojo.LinesEntity;
import org.example.dao.LinesMapper;
import org.example.service.LinesService;
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
public class LinesServiceImpl extends ServiceImpl<LinesMapper, LinesEntity> implements LinesService {

//根据LineID差数据
    @Override
    public LinesEntity lineIdGetLine(String string) {
        QueryWrapper<LinesEntity> queryWrapper = new QueryWrapper<>();
        QueryWrapper<LinesEntity> lineId = queryWrapper.eq("id", string);
        LinesEntity one = getOne(lineId);
        return one;
    }
}
