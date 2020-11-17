package com.company.BIO.TCP.client;

import com.company.BIO.Client;
import com.company.Utils.*;
import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.util.ImageUtils;

import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.*;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.company.Utils.VedioUtil.convertJPGToAvi;
import static com.company.Utils.VedioUtil.convertVedioType;
import static com.company.Utils.VedioUtil.mergeVedioAndAudio;

public class client implements Runnable{
    static String serverName = "127.0.0.1";
    public static JPanel chatPanel;
    int port;
    static Thread  thread;
    static String ClientHost="";
    static JFrame jFrame;
    static JScrollPane Scroll;
    private boolean voiceCheck=true;
    public void updateChat(){
       new Timer().scheduleAtFixedRate(new TimerTask() {
           @Override
           public void run() {
                   chatPanel.updateUI();
//               System.out.println("更新面板");
           }
       }, 0, 10000);
    }
   public  client(String ServerName, int ServerPort, JFrame jFrame){
       client.jFrame =jFrame;
        this.port=ServerPort;
        serverName=ServerName;
    }
    public void sendMessage(DataOutputStream out,JTextField jTextField){
        String word= jTextField.getText();
        try {

            out.writeUTF(word);
            jTextField.setText("");
        }catch (Exception ee){
            System.err.println("服务器断开连接！");
            JOptionPane.showMessageDialog(jFrame, "与服务器丢失连接");//提示框
        }
    }
    public static String getFileSuffixe(File file){
        return file.getName().substring(file.getName().lastIndexOf("."));
    }
    public void sendFileSuffix(String path,DataOutputStream out){
        String word=path.substring(path.lastIndexOf("."));
        try {
            out.writeUTF("filename:"+word);
        }catch (Exception ee){
            System.err.println("服务器断开连接！");
            JOptionPane.showMessageDialog(jFrame, "与服务器丢失连接");//提示框
        }
    }
    private static AudioFormat audioFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
            32000, 16, 1, 2, 32000, true);
    private static DataLine.Info info=new DataLine.Info(TargetDataLine.class,audioFormat);
    private static TargetDataLine targetDataLine;
    private  static SourceDataLine sd;
    public static JFrame real_time_jFrame=new JFrame("实时语音");

    static {
        try {
            sd = (SourceDataLine) AudioSystem.getLine(new DataLine.Info(SourceDataLine.class,audioFormat));
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
    }
    static byte[] SendVoice(int  bytelength)throws Exception{
        String ip= NameUtil.bulid_UDP_Ip(ClientHost);
//        System.err.println("Server to Client:"+ip);
        byte[] b=new byte[bytelength+22];
        char[] chars=ip.toCharArray();
        for(int i=0;i<chars.length;i++){
            b[i]=(byte) chars[i];
        }
        if(targetDataLine!=null&&targetDataLine.isOpen()) {
            targetDataLine.read(b,22,b.length-22);
            return b;
        }else{
            targetDataLine = (TargetDataLine) AudioSystem.getLine(info);
            targetDataLine.open(audioFormat);
            targetDataLine.start();
            targetDataLine.read(b,22,b.length-22);
            return b;
        }

    }
    public void BulidVoicePanel(int bytelength,JLabel jLabel){
        real_time_jFrame.setSize(500,-1);
        voiceCheck=true;
        ExecutorService es=Executors.newCachedThreadPool();
        real_time_jFrame.setVisible(true);
        real_time_jFrame.toFront();
        es.submit(()->{
           byte []buf;
           try(DatagramSocket ds = new DatagramSocket() ) {
               DatagramPacket dp ;
               //2.创建客户端发送数据包
               while(voiceCheck){
                   buf=SendVoice(bytelength);
                   dp = new DatagramPacket(buf, buf.length, InetAddress.getByName(serverName), 11152);
                   ds.send(dp);
               }
           }catch (Exception e){
               e.printStackTrace();
           }
       });
        es.submit(()->{
            Socket s=null;
            try {
                s=new Socket(serverName,11111);
                DataInputStream dis=new DataInputStream(s.getInputStream());
                sd.open(audioFormat);
                sd.start();
                byte b[]=new byte[bytelength];
                while (voiceCheck){
                        dis.read(b,0,b.length);
                        sd.write(b,0,b.length);
                }
                s.close();
            }catch (Exception e){
                try {
                    if(s!=null)
                    s.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                JOptionPane.showMessageDialog(jFrame, "语音服务器断开");//提示框
            }
        });
        real_time_jFrame.addWindowListener(new WindowAdapter() {
           @Override
           public void windowClosing(WindowEvent e) {
               targetDataLine.close();
               voiceCheck=!voiceCheck;
               real_time_jFrame.setVisible(false);
               es.shutdown();
               jLabel.setEnabled(true);
               es.shutdownNow();
               System.gc();
           }
       });

    }
    @Override
    public void run(){
        try
        {
            Socket client= new Socket(serverName, port);
            DataOutputStream out = new DataOutputStream(client.getOutputStream());
            DataInputStream in = new DataInputStream(client.getInputStream());
            System.out.println("连接到主机：" + serverName + " ，端口号：" + port);
            ClientHost=in.readUTF();
            String Name=JOptionPane.showInputDialog("输入你在聊天室的昵称！不要重复哦~");
            while (true){
                out.writeUTF("i'm "+Name);
                String name=in.readUTF();
                System.out.println(name);
                if(Name.trim().equals("")){
                    Name=JOptionPane.showInputDialog("昵称不能为空！");
                }else if(Name==null){
                    try {
                        out.writeUTF("quit");
                        if(thread!=null) thread.interrupt();
                        client.close();
                    } catch (IOException e1) {
                        System.err.println("关闭失败");
                    }
                } else if(name.equals("name error")){
                    Name=JOptionPane.showInputDialog("昵称重复了，请重新输入");
                }else{
                    break;
                }
            }
            JLabel audioJLabel=new JLabel();
            audioJLabel.setCursor(GUIUtil.pointerCursor);
            JLabel voiceJLabel=new JLabel();
            voiceJLabel.setCursor(GUIUtil.pointerCursor);
            JLabel vedioJLabel=new JLabel();
            vedioJLabel.setCursor(GUIUtil.pointerCursor);
            JLabel real_time_vedioJLabel=new JLabel();
            real_time_vedioJLabel.setIcon(new ImageIcon("image/ic_vedio_black.png"));
            vedioJLabel.setIcon(new ImageIcon("image/vedio-validate.png"));
            voiceJLabel.setIcon(new ImageIcon("image/Real-time-voice.png"));
            audioJLabel.setIcon(new ImageIcon("image/voice.png"));
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

                        audioFile= new File("temp/audio/s_"+UUID.randomUUID().toString()+".mp3");
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
                        try {
//                        System.out.println();
                        System.err.println("录音成功!");
                        sendFileSuffix(audioFile.getName()+"||"+ (int)AudioUtil.GetAudioTime(audioFile.getAbsolutePath()),out);
                            Thread.sleep(10);
                        } catch (InterruptedException e1) {
                            e1.printStackTrace();
                        }
                        new  UpdateFile(audioFile.getAbsolutePath(),ClientHost).start();
                    }

                }
            });
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
                        Webcam webcam=VedioUtil.GetWebcam();
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
                                        sendFileSuffix(new File(finalFile).getAbsolutePath(),out);
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
            jButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if(jTextField.getText().length()>65535){
                        JOptionPane.showMessageDialog(jFrame, "输入字数超过限制!");//提示框
                    }else{
                        sendMessage(out,jTextField);
                    }

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
                                    Client.b.setEnabled(true);
                                        System.out.println("closed");
                                     real_time_jFrame.setVisible(false);
                                    voiceCheck=!voiceCheck;
                                        jFrame.setVisible(true);
                                    try {
                                        out.writeUTF("quit");
                                        if(thread!=null)
                                        thread.interrupt();
                                        client.close();
                                    } catch (IOException e1) {
                                        System.err.println("关闭失败");
                                    }
                                }
                            });
            JPanel jPanel1=new JPanel();

            chatPanel=new JPanel();
            jDialog.setMinimumSize(new Dimension(700,500));
            jDialog.setIconImage(Toolkit.getDefaultToolkit().getImage("image/Client.png"));
            chatPanel.setLayout(new GridLayout(0,1));
            Scroll=new JScrollPane(chatPanel,ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

            jTextField.setFont(GUIUtil.font);
            jButton.setFont(GUIUtil.font);
            jPanel1.add(audioJLabel);
            jPanel1.add(jTextField);

            jTextField.setPreferredSize(new Dimension(300,40));
//            jTextArea.setPreferredSize(new Dimension(500,400));
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
                        sendFileSuffix(path,out);
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e1) {
                            e1.printStackTrace();
                        }
                        new  UpdateFile(path,ClientHost).start();

                    }else{
                        System.out.println("取消上传图片");
                    }
                }
            });
            jPanel1.add(UPL);
            jPanel1.add(voiceJLabel);
            jPanel1.add(vedioJLabel);
            jPanel1.add(real_time_vedioJLabel);
            jDialog.add(jPanel1, BorderLayout.SOUTH);
            jDialog.add(Scroll, BorderLayout.CENTER);
            jTextField.addKeyListener(new KeyAdapter() {
                @Override
                public void keyTyped(KeyEvent e) {
                    if(e.getKeyChar()==KeyEvent.VK_ENTER){
                        sendMessage(out,jTextField);
//                        }
                    }
                }
            });
            jDialog.setVisible(true);
            thread=new Thread(new Serverchat(client,chatPanel,jDialog,Scroll));
            thread.start();
            updateChat();
        }catch(Exception e)
        {
            Client.b.setEnabled(true);
            System.err.println("连接失败！");
            if(null!=jFrame)
            JOptionPane.showMessageDialog(jFrame, "警告! 连接失败");//提示框
        }
    }


}





