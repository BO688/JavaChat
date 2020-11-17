package com.company.BIO.TCP.server;


import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 *服务端TCP端口
 * 12345-ServerAcceptBase64
 * 11111-ServerSendVoice
 *
  */
public class server extends Thread{
    private ServerSocket serverSocket;
    private static HashMap<Integer,Class> ThreadMap=new HashMap<>();
    static {
        new ServerAcceptBase64().start();
        new ServerReceiveVoice().start();
        new ServerSendVoice().start();
    }
    public static HashMap<String,Socket> voiceMap=new HashMap<>();
    public  static HashMap<String,Socket> map=new HashMap<>();
    public  static HashMap<String,String> mapFilename=new HashMap<>();
    public  static HashMap<String,String> mapName=new HashMap<>();
    public server(int port) throws IOException
    {
        serverSocket = new ServerSocket(port);
    }
    public void run()
    {
        System.out.println("等待远程连接，端口号为：" + serverSocket.getLocalPort() + "...");
        while(true)
        {
            try
            {
                Socket server = serverSocket.accept();
                String RemoteHost=server.getRemoteSocketAddress().toString();
                new DataOutputStream(  server.getOutputStream()).writeUTF(RemoteHost);
                map.put(RemoteHost,server);
               new Thread(new clientchat(server)).start();
                System.err.println("远程主机地址：" +RemoteHost);
            }catch(Exception s)
            {
                System.out.println("Socket close!");

                break;
            }
        }
    }
    @Override
    public void interrupt() {
        super.interrupt();
        try {
            serverSocket.close();
            if(serverSocket.isClosed()){
                System.out.println("已关闭");
            }else{
                System.out.println("未关闭");
            }
        }catch (Exception e){
            System.err.println("关闭失败");
        }

    }
    public static void sendAll(String word){
        Iterator<Map.Entry<String,Socket>>i=map.entrySet().iterator();
        while (i.hasNext()){
            Map.Entry<String,Socket> MapEntry=i.next();
            Socket SendSocket=MapEntry.getValue();
            if(SendSocket.isClosed()){
                mapName.remove(MapEntry.getKey());
                i.remove();
            }else{
                try {
                    new DataOutputStream(SendSocket.getOutputStream()).writeUTF(word);
                }catch (Exception e){
                    i.remove();
                    mapName.remove(MapEntry.getKey());
                    System.out.println(SendSocket.isOutputShutdown());
                    e.printStackTrace();
                }

            }

        }
    }
//    public static void main(String [] args)throws Exception
//    {
//        int port = Integer.parseInt(args[0]);
//        System.out.println(port);
//            Thread t = new server(port);
//            t.run();
//    }
}

