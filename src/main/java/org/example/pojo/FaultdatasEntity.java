package org.example.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.sql.Blob;
import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.ibatis.type.BlobTypeHandler;

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
@TableName("t_faultdatas")
@ApiModel(value = "FaultdatasEntity对象", description = "")
public class FaultdatasEntity {

    @TableId(value = "id", type = IdType.AUTO)
    private String id;

    @TableField("faultTime")
    private String faultTime;

    @TableField("us")
    private BigDecimal us;

    @TableField("deviceId")
    private Integer deviceId;

    @TableField(value = "`data`", typeHandler = BlobTypeHandler.class)
    private byte[] data;

    @TableField("deviation")
    private Integer deviation;

    @TableField("procTime")
    private String procTime;

    @TableField("lineId")
    private String lineId;

    @TableField("isfault")
    private String isfault;

    @TableField("validData")
    private String validData;

    @TableField("location")
    private BigDecimal location;

    @TableField("reasion")
    private String reasion;

    @TableField("collectFrequency")
    private String collectFrequency;

    @TableField("collectLength")
    private String collectLength;

    @TableField("collectRange")
    private String collectRange;

    @TableField("remark")
    private String remark;

    @TableField("measureType")
    private Integer measureType;

    @TableField("timeType")
    private String timeType;

    @TableField("amplitude")
    private Integer amplitude;

    @TableField("`phase`")
    private String phase;

    @TableField("breakerdesc")
    private String breakerdesc;

    @TableField("triggerType")
    private String triggerType;

    @TableField("isexport")
    private String isexport;

    @TableField("fno")
    private Long fno;


}
