package com.company.BIO;

import com.company.BIO.TCP.client.client;
import com.company.Utils.GUIUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import static java.lang.System.exit;

public class Client {
    public static String Host;
    public  static int Port;
    private static String datafile="data/data.txt";
    private static TrayIcon trayIcon = null;
    static SystemTray tray = SystemTray.getSystemTray();
    static {
        File file=new  File("temp");
        if(!file.exists()){
            file.mkdir();
        }
        File file1=new  File("temp/img");
        if(!file1.exists()){
            file1.mkdir();
        }
        file1=new  File("temp/audio");
        if(!file1.exists()){
            file1.mkdir();
        }
        file1=new  File("temp/vedio");
        if(!file1.exists()){
            file1.mkdir();
        }
    }
    List<String> list1=new LinkedList<>();
    List<String> list2=new LinkedList<>();
    HashMap<String,String> ip_port_map=new HashMap<>();
    Object []list1str=list1.toArray();
    Object []list2str=list2.toArray();
    JComboBox jcb=new JComboBox();
    Thread t;
    final public static JButton b = new JButton("连接服务器");//设置一个按钮
    Client(){
        b.setCursor(GUIUtil.pointerCursor);
        JFrame jFrame = new JFrame("聊天客户器");
        ImageIcon trayImg = new ImageIcon("image/Client.png");// 托盘图标
        PopupMenu popupMenu=new PopupMenu();
        MenuItem mi1=new MenuItem("Show");
        mi1.addActionListener((e)->{
            jFrame.setVisible(true);
            jFrame.setExtendedState(JFrame.NORMAL);
            jFrame.toFront();
        });
        MenuItem mi2;

        mi2 = new MenuItem("Close");

        mi2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                exit(0);
            }
        });
        popupMenu.add(mi1);
        popupMenu.add(mi2);
        popupMenu.setFont(GUIUtil.font);
        trayIcon = new TrayIcon(trayImg.getImage(), "BoClient", popupMenu);
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

        JLabel label1=new JLabel("IP地址:");
        JLabel label2=new JLabel("端口:");
        Host="127.0.0.1";
        Port=688;
        JTextField jTextField2=new JTextField(Port+"");
        jTextField2.setFont(GUIUtil.font);
        label1.setFont(GUIUtil.font);
        label2.setFont(GUIUtil.font);
        label1.setBounds(380,30,70,50);
        label2.setBounds(380,80,50,50);
//        jTextField1.setBounds(450,35,150,40);
        jTextField2.setBounds(450,85,80,40);
        jFrame.setResizable(false);

        jFrame.setSize(760, 350);//窗口的尺寸
        jFrame.setLocation(500,600);//窗口的位置
        jFrame.setLayout(null);//组件释放
        final JLabel k = new JLabel();//标签类实例化一个对象
        ImageIcon i = new ImageIcon("image/Client.png");//在图标类中实例化一个对象并链接上绝对地址
        k.setIcon(i);//定义此组间将要显示的图标
        k.setBounds(50, 50, i.getIconWidth(), i.getIconHeight());//设置图片大小

//        jFrame.add(jTextField1);
        jFrame.add(jTextField2);
        jFrame.add(label1);
        jFrame.add(label2);
        jFrame.add(k);//将标签加到容器中

        b.setBounds(300, 200, 150, 50);//设置这个按钮的位置
        final JButton c = new JButton("保存Session");//设置一个按钮
        c.setCursor(GUIUtil.pointerCursor);
        UpdateDataFileList();
        jcb=UpdateDataFileMapArray(jTextField2,GUIUtil.font);
        c.setBounds(530, 200, 150, 50);//设置这个按钮的位置
        c.setFont(GUIUtil.font);
//        jcb.setFont(GUIUtil.font);
        jFrame.add(c);
        jFrame.add(jcb);
        b.setFont(GUIUtil.font);
//        jcb.setVisible(true);
        b.setFocusPainted(false);
        c.setFocusPainted(false);
        jFrame.add(b);//把琴女的按钮加到容器中
        c.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(jTextField2.getText().equals("")||Host.equals("")){
                    JOptionPane.showMessageDialog(null, "ip和port不能为空");//提示框
                    return;
                }
                try(BufferedReader br=new BufferedReader(new FileReader(datafile))) {
                    String BR="\r\n";
                    boolean check=false;
                    String headStr="";
                    String tailStr="";
                    String target;

                    boolean nextip=false;
                    while ((target=br.readLine())!=null){
                        if(target.trim().equals(Host)){

                            check=true;
                            headStr+=target+BR;
                            continue;
                        }
                        if(check) {
                            if(nextip){
                                tailStr += target + BR;
                            }
                            nextip=true;
                        }else{
                            headStr+=target+BR;
                        }
                    }
                    FileWriter pw;
                   if(check){
                       pw=new FileWriter(datafile);
                       pw.write(headStr);
                       pw.write(jTextField2.getText()+BR);
                       pw.write(tailStr);

                   }else{
                       pw=new FileWriter(datafile,true);
                       pw.append(Host).append("\r\n");
                       pw.append(jTextField2.getText()).append("\r\n");
                   }

                    pw.close();
                   UpdateDataFileList();
                   jFrame.remove(jcb);
                    jcb=UpdateDataFileMapArray(jTextField2,GUIUtil.font);
                   jcb.setVisible(true);
                   jFrame.add(jcb);
                   jcb.updateUI();

                } catch (Exception e1) {
                    e1.printStackTrace();
                    System.err.println("保存session失败");
                    JOptionPane.showMessageDialog(null, "保存session失败");//提示框
                }


            }
        });
        b.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){   //事件响应器，当点击按钮后会执行方法中的代码
                if(b.isEnabled()){
                    b.setEnabled(false);
                    try  {
                        if(jTextField2.getText().equals("")||Host.equals("")){
                            JOptionPane.showMessageDialog(null, "ip和port不能为空");//提示框
                            return;
                        }
                        System.out.println(jcb.getSelectedIndex());
                        if(jcb.getSelectedIndex()!=-1){
                            Host=list1str[jcb.getSelectedIndex()].toString();
                        }
                        System.out.println(Host+":"+Integer.parseInt(jTextField2.getText()));
                        t=new Thread(new client(Host,Integer.parseInt(jTextField2.getText()),jFrame));
                        t.start();

                    }catch (RuntimeException ex){

                        ex.printStackTrace();
                    }

                }

                }


        });
        b.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent event){

                    if (KeyEvent.getKeyText(event.getKeyCode()).compareToIgnoreCase("Enter")==0){
                        b.doClick();
                    }


            }
        });
        jFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                super.windowClosed(e);
                t.interrupt();
            }
        });
        jFrame.setIconImage(Toolkit.getDefaultToolkit().getImage("image/Client.png"));
        b.setToolTipText("连接");//设置一个信息，当鼠标移动到按钮上会显示出来字体
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);//窗口关闭后程序结束
        jFrame.setVisible(true);//窗口可视化
    }
    private  void UpdateDataFileList(){
        try {
            List list11=new LinkedList();
            List list22=new LinkedList();
            BufferedReader br=new BufferedReader(new FileReader(datafile));
            String s;
            boolean check=true;
            while ((s=br.readLine())!=null){
                if(check&&!s.equals("")){
                    list11.add(s);
                }else{
                    list22.add(s);
                }
                check=!check;
            }

            list1=list11;
            list2=list22;
            Collections.reverse(list1);
            Collections.reverse(list2);

            System.out.println(list1);
            System.out.println(list2);
        } catch (Exception e) {
        }
    }
    private  JComboBox UpdateDataFileMapArray(JTextField jTextField2,Font font){
        list1str=list1.toArray();
       list2str=list2.toArray();
        for (int j = 0; j <list1str .length; j++) {
            ip_port_map.put(list1str[j].toString(),list2str[j].toString());
        }
        jTextField2.setText(list2str[0].toString());
       return UpdateComboBox(font,jTextField2);
    }
    private  JComboBox UpdateComboBox(Font font,JTextField jTextField2){

        JComboBox jcb=new JComboBox(list1str);
        jcb.setEditable(true);
        jcb.setBounds(450,35,200,40);
        jcb.setVisible(true);
        jcb.setFont(font);
        jcb.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if(e.getStateChange()==1){
                    String item=e.getItem().toString();
                    System.out.println(item);
                    Host=(item);
                    if(ip_port_map.get(item)!=null){
                        Port=Integer.parseInt(ip_port_map.get(item));
                    }
                    jTextField2.setText(Port+"");
                }

            }
        });
        return jcb;
    }
    public static void main(String[] args) {
        new Client();
    }
}
