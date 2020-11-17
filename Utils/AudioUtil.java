package com.company.Utils;

import java.io.IOException;
import java.io.InputStream;

public class AudioUtil {
    public static float GetAudioTime(String targetName){
        String command="ffmpeg/ffplay.exe -nodisp -an "+targetName;
        try {
            Process process=Runtime.getRuntime().exec(command);
            InputStream inputStream =process.getErrorStream();
            byte[] bytes=new byte[1024];
            String Message="";
            while (inputStream.read(bytes)!=-1){
                System.out.println(new String(bytes));
                Message+=new String(bytes);
                if(Message.contains("Duration")&&Message.contains("bitrate")){
                    System.out.println("获取到时长");break;
                }
            }
          process.destroy();
            return GetTimeSeconds(Message.substring(Message.indexOf("Duration: ")+"Duration: ".length(),Message.indexOf(", bitrate")));
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
    }
    public static float GetTimeSeconds(String Date){
        String []timeArray=Date.split(":");
        return Integer.parseInt(timeArray[0])*60*60+Integer.parseInt(timeArray[1])*60+Float.parseFloat(timeArray[2]);
    }
//    public static void main(String[] args) {
//        System.out.println(GetAudioTime("C:\\Users\\BO\\Desktop\\CS\\temp\\temp.mp3"));
//    }
}
