package org.example.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import io.swagger.annotations.ApiModel;
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
@TableName("t_measurespace")
@ApiModel(value = "MeasurespaceEntity对象", description = "")
public class MeasurespaceEntity {

    @TableId(value = "id", type = IdType.AUTO)
    private String id;

    @TableField("faultTime.txt")
    private String faultTime;

    @TableField("us")
    private BigDecimal us;

    @TableField("lineId")
    private String lineId;

    @TableField("substationA1")
    private String substationA1;

    @TableField("locationA1")
    private BigDecimal locationA1;

    @TableField("substationB1")
    private String substationB1;

    @TableField("locationB1")
    private BigDecimal locationB1;

    @TableField("method1")
    private Integer method1;

    @TableField("faultId1")
    private String faultId1;

    @TableField("faultId2")
    private String faultId2;

    @TableField("procTime")
    private String procTime;

    @TableField("towerLocation")
    private String towerLocation;

    @TableField("reallocation")
    private BigDecimal reallocation;

    @TableField("reason")
    private String reason;

    @TableField("note")
    private String note;

    @TableField("isexport")
    private String isexport;

    @TableField("amplitude")
    private Integer amplitude;

    @TableField("`phase`")
    private String phase;

    @TableField("breakerdesc")
    private String breakerdesc;

    @TableField("measureType")
    private Integer measureType;


}
