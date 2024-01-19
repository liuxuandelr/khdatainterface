package org.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import java.util.Map;
import org.example.pojo.EventsEntity;

public interface EventsService extends IService<EventsEntity> {
    Map<String, EventsEntity> allEventsList(int num);

    EventsEntity outEventsByTimeAndDeviceId();
}
