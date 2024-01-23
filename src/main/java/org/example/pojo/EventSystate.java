package org.example.pojo;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

@Data
public class EventSystate {
    private String id;

    private String time;

    private boolean alm1;

    private boolean alm2;

    private boolean alm3;

    private boolean alm4;
}
