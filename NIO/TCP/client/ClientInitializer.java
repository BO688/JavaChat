package com.company.NIO.TCP.client;

import com.company.Utils.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import javax.swing.*;
import java.applet.AudioClip;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class ClientInitializer extends ChannelInitializer<SocketChannel> {
    Class c;
    public ClientInitializer(Class c){
        this.c=c;
    }
    @Override
    public void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast("framer", new DelimiterBasedFrameDecoder(1024*1024, Delimiters.lineDelimiter()));
        pipeline.addLast("decoder", new StringDecoder());
        pipeline.addLast("encoder", new StringEncoder());
        pipeline.addLast("handler", new ClientHandler(c));
    }
}
class ClientHandler extends SimpleChannelInboundHandler<String> {
    Class c;
    private HashMap<String,String> FileMap =new HashMap<>();
    public ClientHandler(Class c){
        this.c=c;
    }
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String s) throws Exception {
        Client.Msg =s;
        MyLock.Unlock(c);
    }
    private boolean check=false;
    private String s="";
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)throws Exception{
//        System.out.println(msg.toString());
        if(msg.toString().length()>4){
            if(msg.toString().endsWith("OVER")){
                check=true;
                s=s+msg.toString().substring(0,msg.toString().length()-4);
            }else{
                s=s+msg.toString();
            }
        }else{
            s+=msg.toString();
            s=s.substring(0, s.length()-4);
            check=true;
        }



    }
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
//        System.out.println("complete:"+s);
        if(!check){return;}
        if(s.contains("welcome")||s.contains("name error")||s.contains("Host-port:")){
            Client.Msg =s;
            MyLock.Unlock(c);
        }else if(s.startsWith("From")){
            if(FileMap.get(NameUtil.getNameBetweenParam(s)).contains(".mp3")){
                String path= "temp/audio/"+ UUID.randomUUID().toString() +".mp3";
                if(Base64Util.generateImage(s.split("start:")[1],path)){
                    BulidAudioJLabel(new File(path),s.split("start:")[0]+FileMap.get(NameUtil.getNameBetweenParam(s)).replace(" .mp3|||",""));
                    System.out.println("图片保存成功");
                }else{
                    System.out.println("图片保存失败");
                }
            }else{
                switch (FileMap.get(NameUtil.getNameBetweenParam(s))){
                    case ".mp4":{
                        String path= "temp/vedio/"+ UUID.randomUUID().toString() +FileMap.get(NameUtil.getNameBetweenParam(s));
                        if(Base64Util.generateImage(s.split("start:")[1],path)){
                            BulidVedioJabel(new File(path),s.split("start:")[0]);
                            System.out.println("图片保存成功");
                        }else{
                            System.out.println("图片保存失败");
                        }
                        break;
                    }
                    default:{
                        String path= "temp/img/"+ UUID.randomUUID().toString() +FileMap.get(NameUtil.getNameBetweenParam(s));
                        if(Base64Util.generateImage(s.split("start:")[1],path)){
                            BulidJpgJabel(new File(path),s.split("start:")[0]);
                            System.out.println("图片保存成功");
                        }else{
                            System.out.println("图片保存失败");
                        }
                    }
                }
            }


        }else if(s.contains("filename:")){
            System.out.println(s);
            FileMap.put(s.split("]")[0].substring(1),s.split("]")[1].replace("filename:",""));
            System.out.println(s.split("]")[1].replace("filename:",""));
        }
        else if(s.contains("speaking")){
            Client.real_time_jFrame.setTitle(s.replace("speaking",""));
        }
        else{
            buildJLabel(s);
            Client.Msg =s;
        }
        s="";
        check=false;
    }

    public void buildJLabel(JPanel chatJpane, String word) throws InterruptedException {
        JLabel jl=new JLabel(word);
//        jl.setSize();
        jl.setBackground(Color.pink);
        jl.setFont(GUIUtil.font);
        chatJpane.add(jl);
        chatJpane.updateUI();
        Thread.sleep(10);
        JScrollBar scrollBar= Client.Scroll.getVerticalScrollBar();
        scrollBar.setValue(scrollBar.getMaximum());
    }
    public void buildJLabel( String word) throws InterruptedException {
        JLabel jl=new JLabel(word);
//        jl.setSize();
        jl.setBackground(Color.pink);
        jl.setFont(GUIUtil.font);
        Client.chatPanel.add(jl);
        Client.chatPanel.updateUI();
        Thread.sleep(10);
        JScrollBar scrollBar= Client.Scroll.getVerticalScrollBar();
        scrollBar.setValue(scrollBar.getMaximum());
    }
    private void BulidJpgJabel(JPanel chatJpane,File file,String word) throws InterruptedException {
        JLabel jLabel=new JLabel(word);
        jLabel.setCursor(GUIUtil.pointerCursor);
        jLabel.setBackground(Color.pink);
        jLabel.setToolTipText(file.getAbsolutePath());
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
        chatJpane.add(jLabel);
        chatJpane.updateUI();
        Thread.sleep(10);
        JScrollBar scrollBar= Client.Scroll.getVerticalScrollBar();
        scrollBar.setValue(scrollBar.getMaximum());
    }
    private void BulidJpgJabel(File file,String word) throws InterruptedException {
        JLabel jLabel=new JLabel(word);
        jLabel.setCursor(GUIUtil.pointerCursor);
        jLabel.setBackground(Color.pink);
        jLabel.setToolTipText(file.getAbsolutePath());
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
        Client.chatPanel.add(jLabel);
        Client.chatPanel.updateUI();
        Thread.sleep(10);
        JScrollBar scrollBar= Client.Scroll.getVerticalScrollBar();
        scrollBar.setValue(scrollBar.getMaximum());
    }
    private void BulidAudioJLabel(File file,String name) throws InterruptedException {
        JLabel jLabel=new JLabel();
//        jLabel.setForeground(Color.white);
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
                    System.out.println("时长"+Integer.parseInt(jLabel.getText().replace(" .mp3|||||","").split("]")[1]));
                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            System.out.println("停止");
                            AudioStop();
                        }
                    },Integer.parseInt(jLabel.getText().replace(" .mp3|||||","").split("]")[1])*1000);
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
        Client.chatPanel.add(jLabel);
        Client.chatPanel.updateUI();
        Thread.sleep(10);
        JScrollBar scrollBar= Client.Scroll.getVerticalScrollBar();
        scrollBar.setValue(scrollBar.getMaximum());
    }
    private void BulidVedioJabel(File file,String word) throws InterruptedException {
        JLabel jLabel=new JLabel(word);
        jLabel.setCursor(GUIUtil.pointerCursor);
        jLabel.setBackground(Color.pink);
        jLabel.setToolTipText(file.getAbsolutePath());
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
        Client.chatPanel.add(jLabel);
        Client.chatPanel.updateUI();
        Thread.sleep(10);
        JScrollBar scrollBar= Client.Scroll.getVerticalScrollBar();
        scrollBar.setValue(scrollBar.getMaximum());
    }
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        JOptionPane.showMessageDialog(null,"与服务器连接中断，请重新连接");
        cause.printStackTrace();
        ctx.close();
    }
}

