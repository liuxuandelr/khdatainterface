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
@TableName("t_lines")
@ApiModel(value = "LinesEntity对象", description = "")
public class LinesEntity {

    @TableId(value = "id", type = IdType.AUTO)
    private String id;

    @TableField("`name`")
    private String name;

    @TableField("substationAID")
    private String substationAID;

    @TableField("substationBID")
    private String substationBID;

    @TableField("speed")
    private BigDecimal speed;

    @TableField("length")
    private BigDecimal length;

    @TableField("voltagelevelId")
    private String voltagelevelId;

    @TableField("substationABackup")
    private String substationABackup;

    @TableField("substationBBackup")
    private String substationBBackup;

    @TableField("monitorA")
    private String monitorA;

    @TableField("monitorB")
    private String monitorB;

    @TableField("modulus")
    private BigDecimal modulus;

    @TableField("isUse")
    private String isUse;

    @TableField("IsTline")
    private String isTline;

    @TableField("hasSection")
    private String hasSection;

    @TableField("isdircur")
    private String isdircur;


}
