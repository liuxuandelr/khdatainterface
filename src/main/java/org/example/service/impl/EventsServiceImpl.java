package org.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.example.pojo.EventsEntity;
import org.example.dao.EventsMapper;
import org.example.service.EventsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author ruiliu
 * @since 2024-01-15 12:48:18
 */
@Service
public class EventsServiceImpl extends ServiceImpl<EventsMapper, EventsEntity> implements EventsService {

//    按降序查询num个数据，如果时间重复则时间后续+bak，这个时间格式需要截断，所以后面并不重要
    @Override
    public Map<String, EventsEntity> allEventsList(int num) {
        QueryWrapper<EventsEntity> qw = new QueryWrapper<>();
        qw.last("Limit "+num).orderByDesc("time");
        List<EventsEntity> list = list(qw);
        Map<String, EventsEntity> collect = new HashMap<>();
        int n = 1;
        for (EventsEntity eventsEntity : list) {
            if (collect.containsKey(eventsEntity.getTime())){
                collect.put(eventsEntity.getTime()+n+"Bak",eventsEntity);
                n++;
            }
            collect.put(eventsEntity.getTime(),eventsEntity);
        }
        return collect;
    }

    @Override
    public EventsEntity outEventsByTimeAndDeviceId() {

        return null;
    }
}
