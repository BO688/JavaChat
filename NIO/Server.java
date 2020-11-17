package com.company.NIO;

import com.company.Utils.GUIUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.ServerSocket;

import static java.lang.System.exit;

public  class  Server {
    Thread t;
    private static TrayIcon trayIcon = null;
    public static boolean TestVoice=true;
    static SystemTray tray = SystemTray.getSystemTray();
     public  Server(){
         try {
             ServerSocket s1=new ServerSocket(65535);
         } catch (IOException e) {
             return;
         }
         JFrame jFrame = new JFrame("聊天服务器");
         ImageIcon trayImg = new ImageIcon("image/Server.png");// 托盘图标
         PopupMenu popupMenu=new PopupMenu();
         MenuItem mi1=new MenuItem("Show");
         mi1.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 jFrame.setVisible(true);
                 jFrame.setExtendedState(JFrame.NORMAL);
                 jFrame.toFront();
             }
         });
         MenuItem mi2=  new MenuItem("Close");

         mi2.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 exit(0);
             }
         });
         popupMenu.add(mi1);
         popupMenu.add(mi2);
         popupMenu.setFont(GUIUtil.font);
         trayIcon = new TrayIcon(trayImg.getImage(), "BoServer", popupMenu);
         trayIcon.setImageAutoSize(true);
         try {
             trayIcon.addActionListener(new ActionListener() {
                 @Override
                 public void actionPerformed(ActionEvent e) {
                     jFrame.setVisible(true);
                     jFrame.setExtendedState(JFrame.NORMAL);
                     jFrame.toFront();
                 }
             });
             tray.add(trayIcon);
         } catch (AWTException e1) {
         }
         jFrame.addWindowListener(new WindowAdapter() {

             @Override
             public void windowIconified(WindowEvent e) {

                 jFrame.setVisible(false);
             }
         });
         JCheckBox jCheckBox=new JCheckBox("语音测试");
         jCheckBox.setSelected(true);
         jCheckBox.setIcon(new ImageIcon("image/right.png"));
         jCheckBox.addActionListener((e)->{

                     if( jCheckBox.isSelected()){
                         jCheckBox.setIcon(new ImageIcon("image/right.png"));
                         TestVoice=true;
                     }else{
                         jCheckBox.setIcon(new ImageIcon("image/wrong.png"));
                         TestVoice=false;
                     }
         });
        JRadioButton jRadioButton1=new JRadioButton();
        JRadioButton jRadioButton2=new JRadioButton();
        JLabel jLabel3=new JLabel("连接中");
        JLabel jLabel4=new JLabel("未连接");
        ButtonGroup buttonGroup=new ButtonGroup();
        buttonGroup.add(jRadioButton1);
        buttonGroup.add(jRadioButton2);
        JLabel label1=new JLabel("IP地址:");
        JLabel label2=new JLabel("端口:");
        JLabel jTextField1=new JLabel("127.0.0.1");
        JTextField jTextField2=new JTextField("6888");
        jTextField1.setFont(GUIUtil.font);
        jTextField2.setFont(GUIUtil.font);
        label1.setFont(GUIUtil.font);
        label2.setFont(GUIUtil.font);
        label1.setBounds(380,30,70,50);
        label2.setBounds(380,80,50,50);
        jTextField1.setBounds(380+70,35,150,40);
        jTextField2.setBounds(380+70,85,80,40);
        jRadioButton1.setEnabled(false);
        jRadioButton2.setEnabled(false);
        jLabel3.setFont(GUIUtil.font);
        jLabel4.setFont(GUIUtil.font);
        jRadioButton1.setBounds(380,85+40,20,40);
        jRadioButton2.setBounds(380+120,85+40,20,40);
        jLabel3.setBounds(400,85+40,120,40);
        jLabel4.setBounds(400+120,85+40,120,40);
        jRadioButton2.setSelected(true);
        jFrame.setResizable(false);
        jFrame.setIconImage(Toolkit.getDefaultToolkit().getImage("image/Server.png"));
        jFrame.setSize(760, 350);//窗口的尺寸
        jFrame.setLocation(500,600);//窗口的位置
        jFrame.setLayout(null);//组件释放
       final JLabel k = new JLabel();//标签类实例化一个对象
        ImageIcon i = new ImageIcon("image/Server.png");//在图标类中实例化一个对象并链接上绝对地址
        k.setIcon(i);//定义此组间将要显示的图标
        k.setBounds(50, 50, i.getIconWidth(), i.getIconHeight());//设置图片大小
        jFrame.add(jRadioButton1);
        jFrame.add(jRadioButton2);
        jFrame.setIconImage(Toolkit.getDefaultToolkit().getImage("image/Server.png"));
        jFrame.add(jLabel3);
        jFrame.add(jLabel4);
        jFrame.add(jTextField1);
        jFrame.add(jTextField2);
        jFrame.add(label1);
        jFrame.add(label2);
        jFrame.add(k);//将标签加到容器中
        final JButton b = new JButton("启动服务");//设置一个按钮
        b.setBounds(300, 200, 150, 50);//设置这个按钮的位置
        jCheckBox.setBounds(525,200,150,50);
        jCheckBox.setFont(GUIUtil.font);
        jCheckBox.setVisible(true);
        jFrame.add(jCheckBox);
         b.setFont(GUIUtil.font);
        b.setFocusPainted(false);
        jFrame.add(b);//把琴女的按钮加到容器中
        b.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){//事件响应器，当点击按钮后会执行方法中的代码
                try {
                    if(b.getText().equals("停止服务")){

                       t.interrupt();
                        b.setText("启动服务");
                        jRadioButton2.setSelected(true);
                        b.setToolTipText("连接");
                    }else{
                        int port=Integer.parseInt(jTextField2.getText());
                        try {
                            t= new com.company.NIO.TCP.server.Server(port);

                        }catch (Exception ee){
                            JOptionPane.showMessageDialog(jFrame, "警告! 端口："+port+"被占用");//提示框

                            return;
                        }

                        t.start();
                        jRadioButton1.setSelected(true);
                        b.setText("停止服务");
                        b.setToolTipText("断开");

                    }
                }catch (Exception ex){
                    ex.printStackTrace();
                }
            }
        });
        b.setToolTipText("连接");//设置一个信息，当鼠标移动到按钮上会显示出来字体
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);//窗口关闭后程序结束
        jFrame.setVisible(true);//窗口可视化
    }
     public static void main(String[] args) {
        new Server();
    }
}
