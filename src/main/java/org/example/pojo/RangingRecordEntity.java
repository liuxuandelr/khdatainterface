package org.example.pojo;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.sql.Blob;
import java.time.LocalDateTime;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * <p>
 * 
 * </p>
 *
 * @author ruiliu
 * @since 2024-01-15 16:53:46
 */
@Getter
@Setter
@Data
@Accessors(chain = true)
@TableName("ranging_record")
@ApiModel(value = "RangingRecordEntity对象", description = "")
public class RangingRecordEntity {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField("Alm1")
    private boolean alm1;

    @TableField("Alm2")
    private boolean alm2;

    @TableField("Alm3")
    private boolean alm3;

    @TableField("Alm4")
    private boolean alm4;

    @TableField("RcdMade")
    private String rcdMade;

    @TableField("FltNum")
    private Integer fltNum;

    @TableField("FltDiskm")
    private Double fltDiskm;

    @TableField("FlWaveSelfT")
    private LocalDateTime flWaveSelfT;

    private String faultTimeUs;

    @TableField("FlWavePeerT")
    private LocalDateTime flWavePeerT;

    @TableField("FaultA")
    private String faultA;

    @TableField("FaultV")
    private String faultV;

    @TableField("LineLen")
    private String lineLen;

    @TableField("LineName")
    private String lineName;

    @TableField("SubName")
    private String subName;

    @TableField("PeerSubName")
    private String peerSubName;

    @TableField("WaveSpd")
    private String waveSpd;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField("equipment_id")
    private String equipmentId;

    @TableField("sensor_id")
    private String sensorId;

    private String faultId;


}
