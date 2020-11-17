package com.company.Utils;

public class NameUtil {
    public static String replaceNameBetweenParam(String Old,String New){
        int l=Old.indexOf("[");
        int r=Old.indexOf("]");
        if(l==-1||r==-1){throw new IllegalArgumentException("没有同时存在[、]");}
        return Old.substring(0,l+1)+New+Old.substring(r);
    }
    public static String getNameBetweenParam(String Old){
        int l=Old.indexOf("[");
        int r=Old.indexOf("]");
        if(l==-1||r==-1){throw new IllegalArgumentException("没有同时存在[、]");}
        if(l>r){
            System.err.println("[、]顺序错误");
            return "未知";
        }
        return Old.substring(l+1,r);
    }
    public static String bulid_UDP_Ip(String ip){
        if(ip.length()==22){
            return ip;
        }else if(ip.length()>22){
           return ip.substring(0,22);
        }else{
            int length=ip.length();
            for (int i=0;i<22-length;i++){
                ip+="$";
            }
            return ip;
        }
    }
    public static String get_UDP_Ip(String ip){
        while (ip.contains("$")){
            ip=ip.replace("$","");
        }
        return ip;
    }
//    public static void main(String[] args) {
//        System.out.println(replaceNameBetweenParam("1[1]1","hello"));
//        System.out.println(replaceNameBetweenParam("[1]1","hello"));
//        System.out.println(replaceNameBetweenParam("[1]","hello"));
//        System.out.println(getNameBetweenParam("1[1]1"));
//        System.out.println(getNameBetweenParam("[2]1[1]1"));
//        System.out.println(getNameBetweenParam("[1]1"));
////        System.out.println(getNameBetweenParam("hello"));
//    }
}
