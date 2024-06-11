package org.example.device.entity;

import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
public enum DEVICE_STATUS {
    // 0 正常， 1 运行时异常， 2 恢复  3 其他     4 系统中断异常  5 启动时异常
    // 0 正常， 001 配置加载， 002 创建会话=003 会话启动     004 数据接收  005  数据推送

    normal("000000", "正常"),

    device_config_load_fail("001001", "设备配置信息加载失败"),
    main_config_init_fail("001002", "主配置文件设备信息加载失败"),

    conn_recover("002001", "网络通信恢复，重新连接成功"),
    change_inter_success("002002", "更换使能接口成功"),
    client_init_start_fail("002003", "设备运行示例初始化启动失败"),
    device_Acceptor_init_fail("002004", "数据接收服务初始化失败"),
    conn_fail("002005", "设备连接故障"),
    enable_inter_fail("002006", "使能接口连接故障"),
    device_network_normal("002007", "网络连接正常"),
    change_inter_fail("002008", "使能接口更换失败"),
    restart_conn("002009", "重新连接中"),
    restart_conning("0020091", "重新连接中"),
    restart_conn_fail("002010", "重新连接失败"),
    device_network_fail("002011", "网络通信异常：ping失败"),
    device_network_fail2("002012", "网络通信异常：端口通讯失败"),
    change_inter_max("002013", "使能接口更换超过一轮"),


    not_update1("004001", "数据超过一定时间未更新"),
    not_update2("004002", "设备上报周期内数据未上报"),
    not_update3("004004", "单个传感器数据长时间未更新"),
    repeat_his("004003", "数据重复发送历史数据"),


    others("999999", "其他");


    private final String code;
    private final String deviceStatusDesc;


    DEVICE_STATUS(String code, String deviceStatusDesc) {
        this.code = code;
        this.deviceStatusDesc = deviceStatusDesc;
    }


}
