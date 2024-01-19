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
@TableName("t_devices")
@ApiModel(value = "DevicesEntity对象", description = "")
public class DevicesEntity {

    @TableId(value = "id", type = IdType.AUTO)
    private String id;

    @TableField("deviceId")
    private Integer deviceId;

    @TableField("`name`")
    private String name;

    @TableField("protocolType")
    private Integer protocolType;

    @TableField("channelNum")
    private Integer channelNum;

    @TableField("noResponTime")
    private Integer noResponTime;

    @TableField("extParam")
    private String extParam;

    @TableField("faultNum")
    private Integer faultNum;

    @TableField("substationId")
    private String substationId;

    @TableField("collectFrequency")
    private String collectFrequency;

    @TableField("collectLength")
    private String collectLength;

    @TableField("deviceType")
    private String deviceType;

    @TableField("gpsId")
    private String gpsId;

    @TableField("measureType")
    private Integer measureType;

    @TableField("used")
    private String used;

    @TableField("deviceIdB")
    private Integer deviceIdB;


}
