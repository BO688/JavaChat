package com.company.BIO.UDP;

public class server {
    private static byte[] getByte(byte[] B,int from){
        byte b[]=new byte[B.length-from];
        for (int i =from; i <B.length ; i++) {
            b[i-from]=B[i];
        }
        return b;
    }
    private static byte[] getByte(byte[] B,int from,int to){
        byte b[]=new byte[to-from];
        for (int i =from; i <to ; i++) {
            b[i-from]=B[i];
        }
        return b;
    }

//    public static void main(String[] args) throws Exception {
//        byte b[]="1234".getBytes();
//        System.out.println(b.length);
//        System.out.println(new String(getByte(b,1)));
//        System.out.println(getByte(b,1).length);
//        System.out.println(new String(getByte(b,1,2)));
//        System.out.println(getByte(b,1,2).length);
//        //1.创建服务端套接字
//        AudioFormat af= new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
//                8000, 16, 1, 2, 8000, true);
//        DataLine.Info dataLineInfo=new DataLine.Info(SourceDataLine.class,af);
//        SourceDataLine sd=(SourceDataLine) AudioSystem.getLine(dataLineInfo);
//        sd.open(af);
//        sd.start();
//        byte[] buf = new byte[1024+16];
//        try (DatagramSocket ds = new DatagramSocket(11152); ServerSocket ss=new ServerSocket(11111)){
//            DatagramPacket dp;
//            //2.创建接受客户端信息的空数据包
//            while(true) {
//                dp= new DatagramPacket(buf, buf.length,new InetSocketAddress("localhost",11152));
//                //3.接受数据
//                ds.receive(dp);
//                System.out.println(new String(getByte(buf,0,16)));
//                System.out.println(new String(getByte(buf,16)));
//                sd.write(getByte(buf,16),0,1024);
////            System.out.println(new String(buf)+buf.length);
////            brea
////                break;
//            }
//            //7.关闭套接字
//        }catch (IOException e){
//            e.printStackTrace();
//        }
//
//    }

    }

