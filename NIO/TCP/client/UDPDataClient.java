package com.company.NIO.TCP.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import javax.sound.sampled.*;

public class UDPDataClient extends Thread{
    private  NioEventLoopGroup group = new NioEventLoopGroup();
    private static AudioFormat audioFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
            32000, 16, 1, 2, 32000, true);
    private static DataLine.Info info=new DataLine.Info(TargetDataLine.class,audioFormat);
    private static TargetDataLine targetDataLine;
    private  static SourceDataLine sd;
    static {
        try {
            sd = (SourceDataLine) AudioSystem.getLine(new DataLine.Info(SourceDataLine.class,audioFormat));
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
    }
    public static void ClientPlay(byte[] bytes) throws LineUnavailableException {

        sd.open(audioFormat);
        sd.start();
        sd.write(bytes,0,bytes.length);
    }
    @Override
    public void run() {
        // 配置客户端

        Bootstrap b = new Bootstrap();
        b.group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .handler(new ChannelInboundHandlerAdapter() {
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                byte[] b=new byte[1024];
                                ((ByteBuf) msg).readBytes(b);
                               ClientPlay(b);
                            }
                        });
        try {
            ChannelFuture f = b.connect(Client.serverName, 11111).sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
    public  void Stop(){
        sd.close();
        group.shutdownGracefully();
    }
//    public static void main(String[] args) {
//        new UDPDataClient().run();
//    }
}
