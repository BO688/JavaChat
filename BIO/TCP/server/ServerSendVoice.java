package com.company.BIO.TCP.server;

import com.company.BIO.Server;

import javax.swing.*;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.Map;

public class ServerSendVoice extends Thread{
    static ServerSocket ss;

    static {
        try {
            ss = new ServerSocket(11111);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null,"11111端口被占用");
            e.printStackTrace();
        }
    }
    @Override
    public void run() {
        while (true){
            try {
                Socket s =ss.accept();
                server.voiceMap.put(s.getRemoteSocketAddress().toString(),s);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void SendVoice(byte[] b, String fromIp){
//        System.err.println("Client to Server:"+fromIp);
        server.sendAll(server.mapName.get(fromIp)+" speaking");
        Iterator<Map.Entry<String, Socket>> i=server.voiceMap.entrySet().iterator();
        while (i.hasNext()){
            Map.Entry<String,Socket> MapEntry=i.next();
            Socket SendSocket=MapEntry.getValue();
            if(SendSocket.isClosed()||SendSocket.isOutputShutdown()){
                System.out.println("移除");
                i.remove();
            }else{
//                System.out.println(fromIp+":"+MapEntry.getKey());
                if(Server.TestVoice){
                    try {
                        new DataOutputStream(SendSocket.getOutputStream()).write(b,0,b.length);
                    }catch (Exception e){
                        i.remove();
                        return;
                    }
                }else{
                    if(!fromIp.split(":")[0].equals(MapEntry.getKey().split(":")[0])){
                        try {
                            new DataOutputStream(SendSocket.getOutputStream()).write(b,0,b.length);
                        }catch (Exception e){
                            i.remove();
                            System.err.println("用户语音断开");
                            return;
                        }
                    }

                }




            }

        }
    }
}
