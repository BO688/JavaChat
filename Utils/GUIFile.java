package com.company.Utils;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileSystemView;
import java.io.*;

public class GUIFile {
     static int result = 0;
    static JFileChooser fileChooser=new JFileChooser();
    static FileSystemView fsv=FileSystemView.getFileSystemView();
    public static boolean GUI_CopyFile(File in, File out)throws Exception{
        FileInputStream is=new FileInputStream(in);
        FileOutputStream os=new FileOutputStream(out);
        int len=0;
        byte[]b=new byte[1024];
        while ((len=is.read(b))!=-1){
            os.write(b);
        }
        return true;
    }
    public static String GUI_SelectPic()throws Exception{
        fileChooser.setCurrentDirectory(fsv.getHomeDirectory());
        fileChooser.setDialogTitle("请选择要上传文件的路径");
        fileChooser.setApproveButtonText("确定");
//        fileChooser.setFileSelectionMode(JFileChooser.OPEN_DIALOG);
        fileChooser.setAcceptAllFileFilterUsed(false);//取消所有文件上传
        fileChooser.addChoosableFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                String name = f.getName();
                return(name.toLowerCase().endsWith(".gif")||
                        name.toLowerCase().endsWith(".jpg")||
                        name.toLowerCase().endsWith(".bmp")||
                        name.toLowerCase().endsWith(".png")||
                        name.toLowerCase().endsWith(".jpeg"));
            }

            @Override
            public String getDescription() {
                return "图片文件：.gif、 .jpg、 .bmp、 .png、 .jpeg";
            }
        });
        result = fileChooser.showOpenDialog(null);
        if (JFileChooser.APPROVE_OPTION == result) {
            File originFile= fileChooser.getSelectedFile();
//            测试
            return originFile.getAbsolutePath();
//            测试
//            String path=originFile.getPath();
//            int lastindex=originFile.toString().lastIndexOf(".");
//            String last=originFile.toString().substring(lastindex);
//            System.out.println("path: "+path);
//            String newPath="temp/img/"+ UUID.randomUUID().toString()+last;
//            File newFile=new File(newPath);
//            if(GUI_CopyFile(originFile,newFile)){
//                return newPath;
//            }
        }else if(JFileChooser.CANCEL_OPTION== result){
            System.out.println("你取消了上传");
        }
        return null;
    }
    public static void OpenFile(String Absolutepath){
        try {
            Process process=Runtime.getRuntime().exec("cmd /c "+Absolutepath);
            InputStream is =process.getErrorStream();
            byte []b=new byte[1024];
            while (is.available()!=0){
                is.read(b);
                System.out.println(new String(b));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
