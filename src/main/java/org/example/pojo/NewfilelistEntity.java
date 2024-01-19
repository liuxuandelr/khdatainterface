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
@TableName("t_newfilelist")
@ApiModel(value = "NewfilelistEntity对象", description = "")
public class NewfilelistEntity {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("fileName")
    private String fileName;

    @TableField("fileTime")
    private String fileTime;

    @TableField("deviceId")
    private Integer deviceId;

    @TableField("filetype")
    private Integer filetype;


}
