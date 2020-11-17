package com.company.NIO.UDP;

import com.company.Utils.NameUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public final class VoiceMsg {
    /*消息内容*/
    private final ByteBuf msg;
    public String getMsgHeader() {
        return msgHeader;
    }

    @Override
    public String toString() {
       return (msg+msgHeader);
    }

    /*消息id*/
    private final String msgHeader;
    /*消息发送或者接受的时间*/


    public VoiceMsg(String  msgHeader,byte[] msg) {
         this.msgHeader=NameUtil.bulid_UDP_Ip(msgHeader);
//        System.out.println("byte[]"+new String(msg));
//        System.out.println("wrap"+new String(Unpooled.wrappedBuffer(msg).array()));
//        System.out.println("copied"+new String(Unpooled.copiedBuffer(msg).array()));
//        System.out.println(new String(msg));
        this.msg=Unpooled.copiedBuffer(msg);

    }
    public VoiceMsg(String  msgHeader,ByteBuf msg) {
        this.msgHeader=NameUtil.bulid_UDP_Ip(msgHeader);
        this.msg=(msg);
    }
    //返回消息内容
    public ByteBuf getMsg() {
        return msg;
    }




}
