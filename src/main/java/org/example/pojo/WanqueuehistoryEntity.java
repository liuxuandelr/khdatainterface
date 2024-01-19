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
@TableName("t_wanqueuehistory")
@ApiModel(value = "WanqueuehistoryEntity对象", description = "")
public class WanqueuehistoryEntity {

    @TableId(value = "id", type = IdType.AUTO)
    private String id;

    @TableField("substationid")
    private String substationid;

    @TableField("faulttime")
    private String faulttime;

    @TableField("us")
    private BigDecimal us;

    @TableField("computertime")
    private String computertime;

    @TableField("isstart")
    private String isstart;

    @TableField("groupid")
    private String groupid;

    @TableField("isDirsub")
    private String isDirsub;


}
