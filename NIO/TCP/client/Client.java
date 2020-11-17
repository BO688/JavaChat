package com.company.NIO.TCP.client;

import com.company.NIO.UDP.UDPClient;
import com.company.NIO.UDP.VoiceMsg;
import com.company.Utils.GUIFile;
import com.company.Utils.GUIUtil;
import com.company.Utils.MyLock;
import com.company.Utils.VedioUtil;
import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.util.ImageUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.company.Utils.VedioUtil.*;

public class Client implements Runnable{
    public static Channel channel;
    static String serverName = "127.0.0.1";
    public static JPanel chatPanel;
    int port;
    static Thread  thread;
    public static String ClientHost="";
    static JFrame jFrame;
    public static JScrollPane Scroll;
    private UDPClient udpClient;

    private static boolean voiceCheck=true;
    public static void updateChat(){
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                chatPanel.updateUI();
//               System.out.println("更新面板");
            }
        }, 0, 10000);
    }
    public Client(String ServerName, int ServerPort, JFrame jFrame){
        Client.jFrame =jFrame;
        this.port=ServerPort;
        serverName=ServerName;

    }
    public static void ChannelWrite(JTextField jTextField){
        String word= jTextField.getText();
        channel.writeAndFlush(word+"OVER\n");
        jTextField.setText("");
    }
    public static String getFileSuffixe(File file){
        return file.getName().substring(file.getName().lastIndexOf("."));
    }
    public static void sendFileSuffix(String path){
        String word=path.substring(path.lastIndexOf("."));
        channel.writeAndFlush("filename:"+word+"OVER\n");
    }
    private static AudioFormat audioFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
            32000, 16, 1, 2, 32000, true);
    private static DataLine.Info info=new DataLine.Info(TargetDataLine.class,audioFormat);
    private static TargetDataLine targetDataLine;

    public static JFrame real_time_jFrame=new JFrame("实时语音");


    static byte[] SendVoice(int  bytelength)throws Exception{
        byte[] b=new byte[bytelength];
        if(targetDataLine!=null&&targetDataLine.isOpen()) {
            targetDataLine.read(b,0,b.length);
            return b;
        }else{
            targetDataLine = (TargetDataLine) AudioSystem.getLine(info);
            targetDataLine.open(audioFormat);
            targetDataLine.start();
            targetDataLine.read(b,0,b.length);
            return b;
        }

    }
    public  void BulidVoicePanel(int bytelength,JLabel jLabel){
        UDPDataClient udpDataClient=new UDPDataClient();
        udpDataClient.run();
        real_time_jFrame.setSize(500,-1);
        voiceCheck=true;
        ExecutorService es=Executors.newCachedThreadPool();
        real_time_jFrame.setVisible(true);
        real_time_jFrame.toFront();
        es.submit(()->{
            byte []buf;
            Channel channel=null;
            try {
               channel=udpClient.getChannel();
                while(voiceCheck){
                    buf=SendVoice(bytelength);
                    channel.writeAndFlush(new VoiceMsg(ClientHost, buf));
                }
                targetDataLine.close();
            }catch (Exception e){
                e.printStackTrace();
            }
            finally {
                if(channel!=null)
                channel.close();
            }
        });
        real_time_jFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                voiceCheck=!voiceCheck;

                udpDataClient.Stop();
                real_time_jFrame.setVisible(false);
                es.shutdown();
                jLabel.setEnabled(true);
                es.shutdownNow();
            }
        });

    }
    public static volatile String Msg;
    private  void ChannelWriteWithLock(String Msg){
        channel.writeAndFlush(Msg+"OVER\n");
        MyLock.Lock(this.getClass());
    }
    private  void ClientInit() throws LineUnavailableException {
        JLabel vedioJLabel=new JLabel();
        vedioJLabel.setCursor(GUIUtil.pointerCursor);
        vedioJLabel.setIcon(new ImageIcon("image/vedio-validate.png"));
        JLabel audioJLabel=new JLabel();
        audioJLabel.setCursor(GUIUtil.pointerCursor);
        audioJLabel.setIcon(new ImageIcon("image/voice.png"));
        try {
            audioJLabel.addMouseListener(new MouseAdapter() {
                Timer timer;
                long TimeInterval=0;
                AudioFormat audioFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
                        8000, 16, 1, 2, 8000, true);
                DataLine.Info info = new DataLine.Info(TargetDataLine.class,audioFormat);
                final TargetDataLine targetDataLine = (TargetDataLine) AudioSystem.getLine(info);
                ExecutorService ste=Executors.newSingleThreadExecutor();
                File audioFile;
                public void JAudiolabelStart(JLabel jLabel){
                    timer=new Timer();
                    timer.scheduleAtFixedRate(new TimerTask() {
                        int i=0;
                        @Override
                        public void run() {
                            if(++i<=3){
                                jLabel.setIcon(new ImageIcon("image/voice"+i+".png"));
                            }else{
                                i=1;
                                jLabel.setIcon(new ImageIcon("image/voice"+i+".png"));
                            }

                        }
                    },0,500);
                }
                public void JAudiolabelStop(JLabel jLabel){
                    jLabel.setIcon(new ImageIcon("image/voice.png"));
                    timer.cancel();
                }
                @Override
                public void mousePressed(MouseEvent e) {
                    try {
                        targetDataLine.open(audioFormat);
                        targetDataLine.start();
                        AudioInputStream cin = new AudioInputStream(targetDataLine);
                        System.out.println("开始录音");

                        audioFile= new File("temp/audio/s_"+ UUID.randomUUID().toString()+".mp3");
                        if(audioFile.exists()){
                            System.err.println("已经存在");
                        }else{
                            if(audioFile.createNewFile()) System.err.println("创建成功");
                        }
                        System.out.println(audioFile.getName());
                        JAudiolabelStart(audioJLabel);
                        ste.submit(()-> {
                            AudioSystem.write(cin, AudioFileFormat.Type.WAVE, audioFile);
                            return audioFile;
                        });
                        TimeInterval=new Date().getTime();
                        new Timer().schedule(new TimerTask() {
                            @Override
                            public void run() {
                                if(targetDataLine.isOpen()){
                                    targetDataLine.close();
                                    JAudiolabelStop(audioJLabel);
                                }
                            }
                        },60000);
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                }
                @Override
                public void mouseReleased(MouseEvent e) {
                    JAudiolabelStop(audioJLabel);
                    TimeInterval=new Date().getTime()-TimeInterval;
                    targetDataLine.close();
                    if(TimeInterval<2000){
                        try {
                            audioFile.delete();
                        }catch (NullPointerException npe){
                        }finally {
                            JOptionPane.showMessageDialog(null, "太短删除！");//提示框
                        }
                    }else if(audioFile==null){
                        JOptionPane.showMessageDialog(null, "请检查麦克风是否有开启!","提示",JOptionPane.WARNING_MESSAGE);//提示框
                    }else{
                            System.err.println("录音成功!");

                            new UpdateFile(audioFile.getAbsolutePath(),ClientHost).start();
                    }

                }
            });
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        } catch (HeadlessException e) {
            e.printStackTrace();
        }
        vedioJLabel.addMouseListener(new MouseAdapter() {
            JFrame jFrame=new JFrame("视频录制");
            AudioFormat audioFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
                    8000, 16, 1, 2, 8000, true);
            DataLine.Info info = new DataLine.Info(TargetDataLine.class,audioFormat);
            final TargetDataLine targetDataLine = (TargetDataLine) AudioSystem.getLine(info);
            ExecutorService ste=Executors.newSingleThreadExecutor();
            File audioFile;
            @Override
            public void mouseClicked(MouseEvent e) {
                if(!jFrame.isVisible()){
                    Webcam webcam= VedioUtil.GetWebcam();
                    JFrame jFrame=new JFrame("视频录制");
                    WebcamPanel webcamPanel=VedioUtil.GetWebcamPanel(webcam);
                    try {
                        targetDataLine.open(audioFormat);
                    } catch (LineUnavailableException e1) {
                        e1.printStackTrace();
                    }
                    targetDataLine.start();
                    AudioInputStream cin = new AudioInputStream(targetDataLine);
                    System.out.println("开始录音");
                    audioFile= new File("temp/audio/s_"+UUID.randomUUID().toString()+".mp3");
                    if(audioFile.exists()){
                        System.err.println("已经存在");
                    }else{
                        try {
                            if(audioFile.createNewFile()) System.err.println("创建成功");
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                    System.out.println(audioFile.getName());
                    ste.submit(()-> {
                        AudioSystem.write(cin, AudioFileFormat.Type.WAVE, audioFile);
                        return audioFile;
                    });
                    jFrame.add(webcamPanel);
                    List<String> list=  VedioUtil.GetVedioPic(webcam, 1 / 60.0, ImageUtils.FORMAT_JPG);
                    jFrame.setSize(640,380);
                    jFrame.setVisible(true);
                    jFrame.addWindowListener(new WindowAdapter() {
                        @Override
                        public void windowClosing(WindowEvent e) {
                            jFrame.setVisible(false);
                            webcamPanel.getWebcam().close();
                            targetDataLine.close();
                            try {
                                System.out.println(list.size());
                                System.out.println(list);
                                String id=UUID.randomUUID().toString();
                                String target="temp/vedio/"+id+".avi";
                                if(convertJPGToAvi(list,target,0)){
                                    System.err.println("合成成功");
                                    String finalFile="temp/vedio/"+id+".mp4";
                                    if(audioFile==null&&convertVedioType(target,finalFile)){
                                        new UpdateFile(new File(finalFile).getAbsolutePath(),ClientHost).start();
                                    }else if(audioFile!=null&&mergeVedioAndAudio(target,audioFile.getAbsolutePath(),finalFile)){
                                        new UpdateFile(new File(finalFile).getAbsolutePath(),ClientHost).start();
                                    }else{
                                        JOptionPane.showMessageDialog(null,"转换失败");
                                    }

                                }else{
                                    System.err.println("合成失败");
                                }
                            } catch (IOException e1) {
                                e1.printStackTrace();
                            }
                        }
                    });
                }

            }
        });
        JLabel voiceJLabel=new JLabel();
        voiceJLabel.setCursor(GUIUtil.pointerCursor);
        voiceJLabel.setIcon(new ImageIcon("image/Real-time-voice.png"));
        JTextField jTextField=new JTextField("HELLO");
        voiceJLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(voiceJLabel.isEnabled()){
                    BulidVoicePanel(1024,voiceJLabel);
                    voiceJLabel.setEnabled(false);
                }
            }
        });
        JButton jButton=new JButton("发送");
        jButton.setCursor(GUIUtil.pointerCursor);
        jButton.setFocusPainted(false);
        jButton.addActionListener((e)-> {
            if(jTextField.getText().length()>65535){
                JOptionPane.showMessageDialog(jFrame, "输入字数超过限制!");//提示框
            }else{
                ChannelWrite(jTextField);
            }
        });
        jFrame.setVisible(false);
        JFrame jDialog=new JFrame();
        jDialog.setLayout(new BorderLayout());
        jDialog.setTitle("聊天室");
        jDialog.setSize(700,500);
        jDialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                com.company.NIO.Client.b.setEnabled(true);
                System.out.println("closed");
                real_time_jFrame.setVisible(false);
                voiceCheck=!voiceCheck;
                jFrame.setVisible(true);
                if(thread!=null)
                    thread.interrupt();
                group.shutdownGracefully();
            }
        });
        JPanel jPanel1=new JPanel();
        chatPanel=new JPanel();
        jDialog.setMinimumSize(new Dimension(700,500));
        jDialog.setIconImage(Toolkit.getDefaultToolkit().getImage("image/Client.png"));
        chatPanel.setLayout(new GridLayout(0,1));
        JScrollPane logScroll=new JScrollPane(chatPanel,ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        Scroll=logScroll;
        jTextField.setFont(GUIUtil.font);
        jButton.setFont(GUIUtil.font);
        jPanel1.add(audioJLabel);
        jPanel1.add(jTextField);
        jTextField.setPreferredSize(new Dimension(300,40));
        jPanel1.add(jButton);
        JLabel UPL=new JLabel();
        UPL.setCursor(GUIUtil.pointerCursor);
        UPL.setIcon(new ImageIcon("image/uploadPic.png"));
        UPL.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                String path= null;
                try {
                    path = GUIFile.GUI_SelectPic();
                } catch (Exception e1) {
                    JOptionPane.showMessageDialog(null, "上传失败！");//提示框
                }
                if(path!=null){
                    new UpdateFile(path,ClientHost).start();
                }else{
                    System.out.println("取消上传图片");
                }
            }
        });
        jPanel1.add(UPL);
        jPanel1.add(voiceJLabel);
        jPanel1.add(vedioJLabel);
        jDialog.add(jPanel1, BorderLayout.SOUTH);
        jDialog.add(logScroll, BorderLayout.CENTER);
        jTextField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if(e.getKeyChar()==KeyEvent.VK_ENTER){
                    ChannelWrite(jTextField);
//                        }
                }
            }
        });
        jDialog.setVisible(true);
        updateChat();
    }
    private boolean CheckName(String msg){
        return msg.contains("[")||msg.contains("]")||msg.contains("&");
    }
    EventLoopGroup group;
    @Override
    public void run(){
        try
        {
            // 配置客户端
            group = new NioEventLoopGroup();
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new ClientInitializer(this.getClass()));
            ChannelFuture f = b.connect(serverName, port).sync();
            channel = f.channel();
            System.out.println("连接到主机：" + serverName + " ，端口号：" + port);
            String Name=JOptionPane.showInputDialog("输入你在聊天室的昵称！不要重复哦~");
            while (true){
                if(Name==null){
                    if(thread!=null) thread.interrupt();
                    group.shutdownGracefully();
                }else if(Name.trim().equals("")){
                    Name=JOptionPane.showInputDialog("昵称不能为空！");
                    continue;
                }else if(CheckName(Name)){
                    Name=JOptionPane.showInputDialog("昵称不能包含[&]等等符号！");
                    continue;
                }
                ChannelWriteWithLock("i'm "+Name);
                String name=Msg;
                if("name error".equals(name)){
                    Name=JOptionPane.showInputDialog("昵称重复了，请重新输入");
                }else{
                    break;
                }
            }
            udpClient=new UDPClient(new InetSocketAddress(serverName,11152));
            ChannelWriteWithLock("who am i");
            ClientHost=Msg.replace("Host-port:","");
            ClientInit();
        }catch(Exception e)
        {
            e.printStackTrace();
            com.company.NIO.Client.b.setEnabled(true);
            System.err.println("连接失败！");
            if(null!=jFrame)
                JOptionPane.showMessageDialog(jFrame, "警告! 连接失败");//提示框
        }
    }


}





