package com.company.BIO.UDP;


import javax.sound.sampled.*;

public class client {
    private static AudioFormat audioFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
            8000, 16, 1, 2, 8000, true);
    private static DataLine.Info info =new DataLine.Info(TargetDataLine.class,audioFormat);
    private static TargetDataLine targetDataLine;
    private  static SourceDataLine sd;
    static {
        try {
            sd = (SourceDataLine) AudioSystem.getLine(new DataLine.Info(SourceDataLine.class,audioFormat));
            targetDataLine = (TargetDataLine) AudioSystem.getLine(info);
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
    }



    private static byte b[]=new byte[1040];
    static byte[] SendVoice()throws Exception{
        char[] word="/127.0.0.1:66666".toCharArray();
        for(int i=0;i<word.length;i++){
            b[i]=(byte) word[i];
        }
//        Thread.sleep(200);
        if(targetDataLine.isOpen()) {
            System.out.println("继续录音");
            targetDataLine.read(b,16,b.length-16);
            return b;
        }else{
            System.out.println("开始录音");
            targetDataLine.open(audioFormat);
            targetDataLine.start();
            targetDataLine.read(b,16,b.length-16);
            return b;
        }

    }
//    public static void main(String[] args)throws Exception  {
//
//
//
////        exit(0);
//        try(DatagramSocket ds = new DatagramSocket(); Socket s=new Socket("127.0.0.1",11111); DataInputStream dis=new DataInputStream(s.getInputStream())) {
//            DatagramPacket dp ;
//            //2.创建客户端发送数据包
//            Executors.newCachedThreadPool().execute(()->{
//                try {
//                    sd.open(audioFormat);
//                    sd.start();
//                byte b[]=new byte[3036];
//                while (true){
//                    dis.read(b,0,b.length);
//                    sd.write(b,0,b.length);
//                }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            });
//            while(true){
//                byte bb[]=SendVoice();
//                System.out.println(new String(bb));
//                dp = new DatagramPacket(bb, bb.length, InetAddress.getByName("localhost"), 11152);
//                ds.send(dp);
//                String info=new String(dp.getData());
//                if("886".equals(info)) {
//                    break;
//                }
//            }
//        }catch (Exception e){
//            e.printStackTrace();
//        }
//        }

}
