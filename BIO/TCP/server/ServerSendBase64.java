package com.company.BIO.TCP.server;

import javax.swing.*;
import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.Map;

public class ServerSendBase64 extends Thread{
    private String ip;
    private  ServerSocket ss;

    private String base64str;

    ServerSendBase64(String ip,String sb,ServerSocket ss){
        this.ss=ss;
        this.ip=ip;
        this.base64str=sb;
    }
    @Override
    public void run() {
        server.sendAll("file from:"+server.mapName.get(ip));
        Iterator<Map.Entry<String, Socket>> i=server.map.entrySet().iterator();
        while (i.hasNext()){
            Map.Entry<String,Socket> MapEntry=i.next();
            Socket SendSocket=MapEntry.getValue();
            System.out.println(SendSocket.getRemoteSocketAddress().toString());
            if(SendSocket.isClosed()){
                System.out.println("remove");
                i.remove();
            }else{
                try {
                    Socket c=ss.accept();
                    DataOutputStream os=new DataOutputStream(c.getOutputStream());
                    os.writeUTF(("发送的文件长度:"+base64str.length()));
                    if(base64str.length()>65535){
                        for (int ii = 0; ii < Math.ceil(base64str.length()/65535.0); ii++) {
                            if((ii+1)*65535>base64str.length()){
                                os.writeUTF(base64str.substring(ii*65535));
                            }else{
                                os.writeUTF(base64str.substring(ii*65535,(ii+1)*65535));
                            }
                        }
                    }else{
                        os.writeUTF(base64str);
                    }
                } catch (Exception e) {
                    server.sendAll("系统:图片发送异常!");
                    JOptionPane.showMessageDialog(null,"server接受图片异常!");
                }
            }

        }
    }
}

