package com.company.BIO.TCP.client;

import com.company.Utils.GUIUtil;

import javax.swing.*;
import java.awt.*;
import java.io.DataInputStream;
import java.io.InputStream;
import java.net.Socket;

public class Serverchat implements Runnable{
    private Socket server;
    private  JPanel chatJpane;
    private JFrame jFrame;
    private  JScrollPane jScrollPane;
    //    private GridBagConstraints gridBagConstraints=new GridBagConstraints();
    Serverchat(Socket server,JPanel chatJpane,JFrame jFrame,JScrollPane jScrollPane){
        this.jScrollPane=jScrollPane;
        this.jFrame=jFrame;
        this.chatJpane=chatJpane;
        this.server=server;
    }
    @Override
    public void run() {

        try(InputStream inFromServer = server.getInputStream(); DataInputStream in = new DataInputStream(inFromServer)) {

            System.out.println("远程主机地址：" + server.getRemoteSocketAddress());
            while (true){
                try {
                    String word=in.readUTF();
                    client.real_time_jFrame.setTitle("实时语音:");
                   if(word.lastIndexOf("#rootbo688:")!=-1&&word.lastIndexOf("个连接")!=-1){
                        jFrame.setTitle("聊天室("+word.replace("#rootbo688:","")+")");
                    }
                    else if(word.startsWith("filename:")){
                        word=word.replace("filename:","");
                        new ClientAcceptBase64(client.serverName,word).start();
                    }else if(word.endsWith(" speaking")){
                        client.real_time_jFrame.setTitle("实时语音:"+word);
                    }else{
                        buildJLabel(word);
                        Thread.sleep(10);
                        JScrollBar scrollBar=jScrollPane.getVerticalScrollBar();
                        scrollBar.setValue(scrollBar.getMaximum());
                    }
                }catch (Exception e){
                    break;
                }
            }

        }catch (Exception e){
            System.out.println("服务器长时间无响应！连接中断！");
            e.printStackTrace();
        }

    }
    public void buildJLabel(String word){
        JLabel jl=new JLabel(word);
        jl.setSize(100,50);
        jl.setBackground(Color.pink);
        jl.setFont(GUIUtil.font);
        chatJpane.add(jl);
        chatJpane.updateUI();

    }

}
