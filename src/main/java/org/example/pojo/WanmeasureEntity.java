package org.example.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
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
@TableName("t_wanmeasure")
@ApiModel(value = "WanmeasureEntity对象", description = "")
public class WanmeasureEntity {

    @TableId(value = "id", type = IdType.AUTO)
    private String id;

    @TableField("measureTime")
    private String measureTime;

    @TableField("us")
    private BigDecimal us;

    @TableField("startsubstationid")
    private String startsubstationid;

    @TableField("substationA")
    private String substationA;

    @TableField("locationA")
    private BigDecimal locationA;

    @TableField("substationB")
    private String substationB;

    @TableField("locationB")
    private BigDecimal locationB;

    @TableField("faultlineid")
    private String faultlineid;

    @TableField("details")
    private String details;

    @TableField("towerlocation")
    private String towerlocation;

    @TableField("faultId1")
    private String faultId1;

    @TableField("faultId2")
    private String faultId2;

    @TableField("`phase`")
    private String phase;

    @TableField("reasion")
    private String reasion;

    @TableField("isexport")
    private String isexport;

    @TableField("amplitude")
    private Integer amplitude;

    @TableField("breakerdesc")
    private String breakerdesc;

    @TableField("measuretype")
    private Integer measuretype;


}
