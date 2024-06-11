package org.example.monitor.status;

public enum IEDStatusEnum {
    Connected,
    ConnectError,
    ChannelSuccess,
    ChannelFail,
    Init;


    private String desc;


    IEDStatusEnum() {
    }

    IEDStatusEnum(String desc) {
        this.desc = desc;
    }

    public static IEDStatusEnum getIedStatus() {


        return Connected;
    }

    public static void main(String[] args) {

        System.out.println(IEDStatusEnum.ChannelFail);
        System.out.println(IEDStatusEnum.ChannelFail.name());
        System.out.println(IEDStatusEnum.ChannelFail.toString());
    }


}
