package com.company.BIO.TCP.client;

import com.company.BIO.Client;
import com.company.Utils.Base64Util;

import javax.swing.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.net.SocketException;

public class UpdateFile extends Thread{
    private String path;
    private String iport;
    UpdateFile(String path,String iport){
        this.path=path;
        this.iport=iport;
    }
    @Override
    public void run() {
        try(Socket c=new Socket(Client.Host,12345)) {
            DataInputStream dis=new DataInputStream(c.getInputStream());
            String base64= Base64Util.encodeBase64File(path).getBase64();
            DataOutputStream os=new DataOutputStream(c.getOutputStream());
            os.writeUTF(iport+"&&"+base64.length());
            if(base64.length()>65535){
                for (int i = 0; i < Math.ceil(base64.length()/65535.0); i++) {
                    if((i+1)*65535>base64.length()){
                        os.writeUTF(base64.substring(i*65535));
                    }else{
                        os.writeUTF(base64.substring(i*65535,(i+1)*65535));
                    }
                }
            }else{
                os.writeUTF(base64);
            }
            try {
                if(dis.readUTF().equals("ok")){
                    JOptionPane.showMessageDialog(null, "成功发送");//提示框
                    os.close();
                }
            }catch (SocketException se){
                System.out.println("接受图片进程！");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "发送异常");//提示框
            e.printStackTrace();
            System.err.println("找不到图片异常!");
        }

    }
}