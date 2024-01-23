package org.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.dao.EventsMapper;
import org.example.pojo.EventSystate;
import org.example.pojo.EventsEntity;
import org.example.service.EventsService;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    private final SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
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

    @Override
    public EventSystate intgetMaxTime() {
        QueryWrapper<EventsEntity> qw = new QueryWrapper<>();
        qw.last("Limit 10").orderByDesc("time");
        List<EventsEntity> list = list(qw);
        List<EventsEntity> collect = list.stream().sorted(Comparator.comparing(EventsEntity::getTime).reversed()).collect(Collectors.toList());
        for (int s = 0 ; s < collect.size() ;s++){
            EventsEntity eventsEntity = list.get(s);
            if (eventsEntity.getContent().contains("双端测距")){
                EventSystate events = new EventSystate();
                events.setTime(collect.get(s).getTime());
                events.setId(collect.get(s).getId());
                events.setAlm1(false);
                events.setAlm2(false);
                events.setAlm3(false);
                events.setAlm4(false);
                return events;
            }
        }
        return null;
    }
}
