package org.example.service;

import org.example.pojo.LinesEntity;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author ruiliu
 * @since 2024-01-15 12:48:18
 */
@Service
@Component
public interface LinesService extends IService<LinesEntity> {

    public LinesEntity lineIdGetLine(String string);
}
