package com.company.BIO.TCP.server;



import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class ServerReceiveVoice extends Thread{
    private static String getTrueIp(String ip){
        while (ip.indexOf("$")!=-1){
            ip=ip.replace("$","");
        }
        return ip;
    }
    private static byte[] getByte(byte[] B,int from){
        byte b[]=new byte[B.length-from];
        for (int i =from; i <B.length ; i++) {
            b[i-from]=B[i];
        }
        return b;
    }
    private static byte[] getByte(byte[] B,int from,int to){
        byte b[]=new byte[to-from];
        for (int i =from; i <to ; i++) {
            b[i-from]=B[i];
        }
        return b;
    }
    @Override
    public void run() {
        byte[] buf = new byte[1024+22];
        try (DatagramSocket ds = new DatagramSocket(11152)){
            DatagramPacket dp;
            //2.创建接受客户端信息的空数据包
            while(true) {
                dp= new DatagramPacket(buf, buf.length);
                //3.接受数据
                ds.receive(dp);
                if( server.voiceMap.size()>=1){
                    ServerSendVoice.SendVoice(getByte(buf,22),getTrueIp(new String(getByte(buf,0,22))));
                }
            }
            //7.关闭套接字
        }catch (IOException e){
            e.printStackTrace();
        }
    }

  


}
