package com.company.BIO.TCP.server;

import cn.hutool.core.util.NetUtil;

import java.net.ServerSocket;
import java.util.*;

public class Master extends TimerTask{
    private HashMap<Integer,Class > map;
    Master(HashMap<Integer,Class > map){
        this.map=map;
    }
    @Override
    public void run() {
        Iterator<Map.Entry<Integer,Class >> iterator =map.entrySet().iterator();
        while (iterator.hasNext()){
            Map.Entry<Integer,Class > MapEntry=iterator.next();
            if(NetUtil.isUsableLocalPort(MapEntry.getKey())){
                Thread t;
                try {
                    t =(Thread) MapEntry.getValue().newInstance();
                } catch (Exception e1) {
                    e1.printStackTrace();
                  continue;
                }
                t.start();
            }
            }
    }

//    public static void main(String[] args) {
//        HashMap <Integer,Class > map=new HashMap();
//        map.put(SocketTest.port,SocketTest.class);
//        new Timer().scheduleAtFixedRate(new Master(map),100,2000);
//    }
}
class SocketTest extends Thread{
    public static int port=12315;
    @Override
    public void run() {
        try {
            ServerSocket ss=new ServerSocket(port);
            System.out.println("开启12315");
            Thread.sleep(1000);
            System.out.println(  "管钱"+NetUtil.isUsableLocalPort(port));
            ss.close();
            System.out.println( "观后"+ NetUtil.isUsableLocalPort(port));

            System.out.println("异常终止");
        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}
