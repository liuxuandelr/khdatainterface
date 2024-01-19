package org.example.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * <p>
 * 
 * </p>
 *
 * @author ruiliu
 * @since 2024-01-15 12:48:18
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("t_devicelinerelation")
@ApiModel(value = "DevicelinerelationEntity对象", description = "")
public class DevicelinerelationEntity {

    @TableId(value = "id", type = IdType.AUTO)
    private String id;

    @TableField("deviceId")
    private String deviceId;

    @TableField("lineId")
    private String lineId;

}
