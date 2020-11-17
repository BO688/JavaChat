package com.company.BIO.TCP.client;

import com.company.Utils.Base64Util;
import com.company.Utils.GUIFile;
import com.company.Utils.GUIUtil;

import javax.swing.*;
import java.applet.AudioClip;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class ClientAcceptBase64 extends Thread{

    private JLabel BulidJpgJabel(JLabel jLabel) {
        ImageIcon II=new ImageIcon("image/img.png");
        II.setImage(II.getImage().getScaledInstance(-1,-1, Image.SCALE_DEFAULT));
        jLabel.setIcon(II);
        jLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                File file=new File(jLabel.getToolTipText());
                if(file.exists()){
                    GUIFile.OpenFile(file.getAbsolutePath());
                }else{
                    JOptionPane.showMessageDialog(null, "无法找到图片路径");//提示框
                    System.err.println("not found");
                }
            }
        });
        return jLabel;
    }
    private JLabel BulidVedioJabel(JLabel jLabel) {
        ImageIcon II=new ImageIcon("image/vedio-validate.png");
        II.setImage(II.getImage().getScaledInstance(-1,-1, Image.SCALE_DEFAULT));
        jLabel.setIcon(II);
        jLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                File file=new File(jLabel.getToolTipText());
                if(file.exists()){
                   GUIFile.OpenFile(jLabel.getToolTipText());
                }else{
                    JOptionPane.showMessageDialog(null, "无法找到图片路径");//提示框
                    System.err.println("not found");
                }
            }
        });
        return jLabel;
    }
    private void BulidJLabel(File file){
        JLabel jLabel=new JLabel();
        jLabel.setCursor(GUIUtil.pointerCursor);
        jLabel.setSize(100,50);
        jLabel.setBackground(Color.pink);
        jLabel.setToolTipText(file.getAbsolutePath());
        switch (client.getFileSuffixe(file)){
            case ".gif":
                jLabel=BulidJpgJabel(jLabel);
                break;
            case ".jpg":
                jLabel=BulidJpgJabel(jLabel);
                break;
            case ".bmp":
                jLabel=BulidJpgJabel(jLabel);
                break;
            case ".png":
                jLabel=BulidJpgJabel(jLabel);
                break;
            case ".jpeg":
                jLabel=BulidJpgJabel(jLabel);
                break;
            case ".avi":
                jLabel=BulidVedioJabel(jLabel);
                break;
            case ".mp4":
                jLabel=BulidVedioJabel(jLabel);
                break;
            default:
        }
        client.chatPanel.add(jLabel);
        client.chatPanel.updateUI();
        new UpdateScroll(client.Scroll).start();
    }
    private void BulidAudioJLabel(File file,String name){
        JLabel jLabel=new JLabel();
        jLabel.setForeground(Color.white);
        jLabel.setToolTipText(file.getAbsolutePath());
        try {
            ImageIcon II=new ImageIcon("image/audio.png");
            jLabel.setIcon(II);
            jLabel.setText(name);
            jLabel.addMouseListener(new MouseAdapter() {
                boolean play=false;
                Timer timer;
                AudioClip ac;
                public void AudioStart(){
                    play=true;
                    timer=new Timer();
                    timer.scheduleAtFixedRate(new TimerTask() {
                        int i=0;
                        @Override
                        public void run() {
                            if(++i<=4){
                                jLabel.setIcon(new ImageIcon("image/audio"+i+".png"));
                            }else{
                                i=1;
                                jLabel.setIcon(new ImageIcon("image/audio"+i+".png"));
                            }
                        }
                    },0,500);
                    ac=loadSound(jLabel.getToolTipText());
                    ac.play();
                    System.out.println("时长"+Integer.parseInt(jLabel.getText()));
                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            System.out.println("停止");
                            AudioStop();
                        }
                    },Integer.parseInt(jLabel.getText())*1000);
                }
                public void AudioStop(){
                    timer.cancel();
                    jLabel.setIcon(new ImageIcon("image/audio.png"));
                    ac.stop();
                    play=false;
                }
                public  AudioClip loadSound(String filename) {
                    URL url = null;
                    try {
                        url = new URL("file:" + filename);
                    }catch ( IOException e) {
                    }
                    return JApplet.newAudioClip(url);
                }
                @Override
                public void mouseClicked(MouseEvent e) {
                    if(play){
                        AudioStop();
                    }else{
                        AudioStart();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        client.chatPanel.add(jLabel);
        client.chatPanel.updateUI();
        new UpdateScroll(client.Scroll).start();
    }

    private String ip;
    private String suffix;
    ClientAcceptBase64(String ip,String suffix){
        this.ip=ip;
        this.suffix=suffix;
    }
    @Override
    public void run() {
        try (Socket s =new Socket(ip,20202);
             DataInputStream is=new DataInputStream(s.getInputStream())){
            String path;
            if(suffix.contains(".mp3||")){
                path="temp/audio/"+ "save_"+ UUID.randomUUID()+".mp3";
            }else if(suffix.contains(".mp4")){
                path="temp/vedio/"+ "save_"+ UUID.randomUUID()+".mp4";
            }else if(suffix.contains(".avi")){
                path="temp/vedio/"+ "save_"+ UUID.randomUUID()+".avi";
            }else{
                path="temp/img/"+ "save_"+ UUID.randomUUID()+suffix;
            }
            String  message=is.readUTF();
            String Base64Str="";
            message=message.replace("发送的文件长度:","");
            int totalsize=Integer.valueOf(message);
            while (totalsize!=Base64Str.length()){
                    message=is.readUTF();
                    Base64Str+=message;
            }
            File targetFile=new File(path);
            Base64Util.decoderBase64File(Base64Str,targetFile);
            if(suffix.contains(".mp3||")){
                BulidAudioJLabel(targetFile,suffix.replace(".mp3||",""));
            }else{
                BulidJLabel(targetFile);
            }
        }catch (Exception eof){
            eof.printStackTrace();
        }
    }
}