package com.company.BIO.TCP.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

public class clientchat implements Runnable{
    private Socket client;
    clientchat(Socket client){
        this.client=client;
    }
    @Override
    public void run() {
        try (DataInputStream in = new DataInputStream(client.getInputStream()); DataOutputStream out = new DataOutputStream(client.getOutputStream())){
            String RemoteHost=client.getRemoteSocketAddress().toString();
            String  word;
            while (true){
                word=in.readUTF();
//                Server.sendAll("");
                if(!word.equals("i'm")){
                    if(word.trim().equals("i'm")||word.trim().equals("i'm ")||word.trim().equals("i'm null")){
                        out.writeUTF("name error");
                    }else{
                        word=word.replace("i'm","");
                        synchronized (this){
                            if(server.mapName.containsValue(word)){
                                out.writeUTF("name error");
                            }else{
                                server.mapName.put(RemoteHost,word);
                                RemoteHost=word;
                                out.writeUTF("name ok");
                                break;
                            }
                        }
                    }

                }else{
                    out.writeUTF("name error");
                }
            }

            out.writeUTF("welcome  to JavaChat!");

            server.sendAll(RemoteHost+"加入了");
            server.sendAll("#rootbo688:现在有"+server.map.size()+"个连接");
            while (true){
                word =in.readUTF();
                if("quit".equals(word)){
                    client.close();
                    server.sendAll(RemoteHost+"退出了");
                    server.sendAll("#rootbo688:现在有"+server.map.size()+"个连接");
                    break;
                }else if(word.equals("")){
                    continue;
                }else if(word.startsWith("filename:")){
                    word=word.replace("filename:","");
                    String ip=client.getRemoteSocketAddress().toString();
                    server.mapFilename.put(ip,word);
                    System.out.println("input:"+word);
                    server.sendAll("filename:"+word);
                }
                else{
                    server.sendAll(RemoteHost+":"+word);
                }
                System.out.println(word+" from "+RemoteHost);
            }

        }catch (Exception e){
            System.err.println("用户超时，连接断开！");

        }
    }
}