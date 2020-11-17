package com.company.Utils;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.WebcamResolution;
import com.github.sarxos.webcam.WebcamUtils;
import com.sun.istack.internal.NotNull;
import org.jim2mov.core.DefaultMovieInfoProvider;
import org.jim2mov.core.Jim2Mov;
import org.jim2mov.core.MovieInfoProvider;
import org.jim2mov.core.MovieSaveException;
import org.jim2mov.utils.MovieUtils;

import javax.imageio.IIOException;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static java.lang.Thread.currentThread;

public class VedioUtil {
    private static Webcam webcam = null;
    public static Webcam GetWebcam(){
        if(webcam!=null){
            return webcam;
        }
        webcam=Webcam.getDefault();
        webcam.setViewSize(WebcamResolution.VGA.getSize());
        return webcam;
    }
    public static WebcamPanel GetWebcamPanel(){
        WebcamPanel panel = new WebcamPanel(GetWebcam());
        panel.setFPSDisplayed(true);
        panel.setDisplayDebugInfo(true);
        panel.setImageSizeDisplayed(true);
        panel.setMirrored(true);
        return panel;
    }
    public static WebcamPanel GetWebcamPanel(Webcam webcam){
        WebcamPanel panel = new WebcamPanel(webcam);
        panel.setFPSDisplayed(true);
        panel.setDisplayDebugInfo(true);
        panel.setImageSizeDisplayed(true);
        panel.setMirrored(true);
        return panel;
    }

    public static List<String> GetVedioPic(@NotNull Webcam webcam,double period,String ImageFormat){
        List <String> list=new LinkedList<>();
        File FileFather=new File("JPG2Vedio");
        if(!FileFather.exists()){
            FileFather.mkdir();
        }
        File FileDir=new File(FileFather.getName()+File.separator+String.valueOf(currentThread().getId()));
        FileDir.mkdir();
        Timer timer=new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                String path=String.valueOf(UUID.randomUUID().toString());
                String filename=FileDir.getAbsolutePath()+File.separator+path;
                try {
                    Thread.sleep(10);
                    WebcamUtils.capture(webcam,filename, ImageFormat);
                }catch (IllegalArgumentException e){
                    System.out.println(e.getMessage());
//                    timer.purge()
                }catch (Exception ee){
                    timer.cancel();
                }
//                new File(filename+"."+ImageFormat).setReadable(true);
                list.add(filename+"."+ImageFormat);

            }
        },0,(int)(period*1000));
        return list;
    }


    /**
     * 将图片转换成视频
     * @param jpgDirPath jpg图片文件夹绝对路径
     * @param aviFileName 生成的avi视频文件名
     * @param fps 每秒帧数
     * @param mWidth 视频的宽度
     * @param mHeight 视频的高度
     * @throws Exception
     */
    public static boolean convertJPGToAvi(@NotNull String jpgDirPath, @NotNull String aviFileName, int fps, int mWidth, int mHeight) {
        final File[] jpgs = new File(jpgDirPath).listFiles();
        if(jpgs==null || jpgs.length==0){
            return false;
        }
        // 对文件名进行排序(本示例假定文件名中的数字越小,生成视频的帧数越靠前)
        Arrays.sort(jpgs, (file1,file2)-> {
            String numberName1 = file1.getName().replace(".jpg", "");
            String numberName2 = file2.getName().replace(".jpg", "");
            return new Integer(numberName1) - new Integer(numberName2);
        });

        // 生成视频的名称
        DefaultMovieInfoProvider dmip = new DefaultMovieInfoProvider(aviFileName);
        // 设置每秒帧数
        dmip.setFPS(fps>0?fps:24); // 如果未设置，默认为3
        // 设置总帧数
        dmip.setNumberOfFrames(jpgs.length);
        // 设置视频宽和高（最好与图片宽高保持一直）
        dmip.setMWidth(mWidth>0?mWidth:1440); // 如果未设置，默认为1440
        dmip.setMHeight(mHeight>0?mHeight:860); // 如果未设置，默认为860
        try {
            new Jim2Mov((frame)->{
                try {
                    // 设置压缩比
                    return MovieUtils.convertImageToJPEG((jpgs[frame]), 1.0f);
                } catch (IOException e) {
                    System.err.println(e);
                }
                return null;
            }, dmip, null).saveMovie(MovieInfoProvider.TYPE_AVI_MJPEG);
            return true;
        } catch (MovieSaveException e) {
            System.err.println(e);
            return false;
        }

    }
    public static boolean convertJPGToAvi(@NotNull File[] SortedDir, @NotNull String aviFileName, int fps, int mWidth, int mHeight) {

        if(SortedDir==null || SortedDir.length==0){
            return false;
        }
        // 生成视频的名称
        DefaultMovieInfoProvider dmip = new DefaultMovieInfoProvider(aviFileName);
        // 设置每秒帧数
        dmip.setFPS(fps>0?fps:24); // 如果未设置，默认为3
        // 设置总帧数
        dmip.setNumberOfFrames(SortedDir.length);
        // 设置视频宽和高（最好与图片宽高保持一直）
        dmip.setMWidth(mWidth>0?mWidth:1440); // 如果未设置，默认为1440
        dmip.setMHeight(mHeight>0?mHeight:860); // 如果未设置，默认为860
        try {
            new Jim2Mov((frame)->{
                try {
                    // 设置压缩比
                    return MovieUtils.convertImageToJPEG(SortedDir[frame], 1.0f);
                } catch (IOException e) {
                    System.err.println(e);
                }
                return null;
            }, dmip, null).saveMovie(MovieInfoProvider.TYPE_AVI_MJPEG);
            return true;
        } catch (MovieSaveException e) {
            System.err.println(e.getMessage());
            return false;
        }

    }
    public static boolean convertJPGToAvi(@NotNull List SamePicPatternList, @NotNull String aviFileName, int fps) throws IOException {
        if(SamePicPatternList==null || SamePicPatternList.size()==0){
            return false;
        }
        // 生成视频的名称
        DefaultMovieInfoProvider dmip = new DefaultMovieInfoProvider(aviFileName);
        // 设置每秒帧数
        dmip.setFPS(fps>0?fps:15);// 如果未设置，默认为3
        // 设置总帧数
        BufferedImage bufferedImage=null;
        int Height;
        int Width;
        try {
            System.out.println(SamePicPatternList.get(0).toString());
           bufferedImage=ImageIO.read(new FileInputStream(SamePicPatternList.get(0).toString()));
           Height=bufferedImage.getHeight();
           Width=bufferedImage.getWidth();
        }catch (IIOException e){
            Height=680;
            Width=340;
        }


        dmip.setMHeight(Height);
        dmip.setMWidth(Width);
        dmip.setNumberOfFrames(SamePicPatternList.size());
        // 设置视频宽和高（最好与图片宽高保持一直）

        try {
            new Jim2Mov((frame)->{
                try {
                    // 设置压缩比
                    return MovieUtils.convertImageToJPEG(new File(SamePicPatternList.get(frame).toString()), 1.0f);
                } catch (IOException e) {
                    System.err.println(e);
                }
                return null;
            }, dmip, null).saveMovie(MovieInfoProvider.TYPE_AVI_MJPEG);
            return true;
        } catch (MovieSaveException e) {
            System.err.println(e.getMessage());
            return false;
        }

    }
    public static boolean convertVedioType(@NotNull String sourceName,@NotNull String targetName){
        String command="ffmpeg/ffmpeg.exe -i "+sourceName+" -y -b:v 640k "+targetName;
        try {
            Process process=Runtime.getRuntime().exec(command);
            InputStream inputStream =process.getErrorStream();
            byte[] bytes=new byte[1024];
            while (inputStream.read(bytes)!=-1){
                System.out.println(new String(bytes));
            }
            inputStream.close();
            if(new File(targetName).exists()){
                new File(sourceName).delete();
                return true;
            }else{
                return false;
            }


        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    public static boolean mergeVedioAndAudio(@NotNull String sourceVedio,@NotNull String sourceAudio,@NotNull String targetName){
        String command="ffmpeg/ffmpeg.exe -i "+sourceVedio +" -i " +sourceAudio+" -y -b:v 640k "+targetName;
        try {
            Process process=Runtime.getRuntime().exec(command);
            InputStream inputStream =process.getErrorStream();
            byte[] bytes=new byte[1024];
            while (inputStream.read(bytes)!=-1){
                System.out.println(new String(bytes));
            }
            inputStream.close();
            if(new File(targetName).exists()){
                new File(sourceVedio).delete();
                new File(sourceAudio).delete();
                return true;
            }else{
                return false;
            }


        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    public static File[] convertObjectA2FileA(Object[] objects){
        System.out.println(objects.length);
        File []files=new File[objects.length];
        int count=0;
        for (Object object:objects
             ) {
            files[count++]=(File)object;        }
        return files;
    }
    public static void startVedioWithFfpay(String targetName){
        String command=new File("ffmpeg/ffplay.exe").getAbsolutePath()+"  -autoexit  -fflags nobuffer  -analyzeduration 1000000  -i "+new File(targetName).getAbsolutePath();
        try {
            Runtime.getRuntime().exec(command);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
//    public static void main(String[] args) throws IOException {
//        Webcam webcam=GetWebcam();
////        System.out.println(new Date().getSeconds());
//        List<String> list=GetVedioPic(webcam, 1/60.0,ImageUtils.FORMAT_JPG);
//        System.err.println("开启图片拍摄,quit退出");
//        while (!new Scanner(System.in).nextLine().equals("quit")){
//            System.err.println("正在拍摄,quit退出");
//        }
//        webcam.close();
////        System.out.println(new Cal);
//        System.out.println(list.size());
//        System.err.println("拍摄完成");
//        System.err.println("开始合成视频");
//        if(convertJPGToAvi(list,"test.avi",0)){
//            System.err.println("合成成功");
//            startVedioWithFfpay("test.avi");
////            if(convertVedioType("test.avi","tt.mp4")){
////                System.out.println("成功！");
////            }else{
////                System.out.println("失败！");
////            }
//        }else{
//            System.err.println("合成失败");
//        }
//
//    }
}
