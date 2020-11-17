package com.company.NIO.TCP.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

public class AcceptFileS extends Thread{
    private int port;
    AcceptFileS(int port){
        this.port=port;
    }
    @Override
    public void run() {
        EventLoopGroup bossGroup = new NioEventLoopGroup(); // (1)
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap(); // (2)
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class) // (3)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch)  {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast("framer", new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter()));
                            pipeline.addLast("decoder", new StringDecoder());
                            pipeline.addLast("encoder", new StringEncoder());
                            pipeline.addLast("handler", new SimpleChannelInboundHandler<String>() {
                                private String Host="";
                                private String Base64="";
                                private boolean Send=false;
                                @Override
                                protected void channelRead0(ChannelHandlerContext ctx, String s) throws Exception {
                                    System.out.println("Read0"+s);
                                }

                                @Override
                                public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                    String s=msg.toString();
                                    if (s.contains("&&&&&")){
                                        String[] strs=s.split("&&&&&");
                                        Host=strs[0];
                                        Base64=strs[1];
                                    }else {
                                        if(s.length()>4){
                                            if(s.endsWith("OVER")){
                                                Send=true;
                                                Base64 +=s.substring(0,s.length()-4);
                                            }else{
                                                Base64 += s;
                                            }
                                        }else{
                                            Send=true;
                                            Base64+=s;
                                            Base64=Base64.substring(0, s.length()-4);
                                        }
                                    }
                                    channelReadComplete(ctx);

                                }
                                @Override
                                public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
                                    if(Send){
                                        Send=false;
//                                        System.err.println("AcceptFileS:"+Base64);
                                        ServerHandler.SendAllPic(Host,Base64);
                                    }

                                }

                                public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                                    cause.printStackTrace();
                                    ctx.close();
                                }
                            });
                        }
                    })  //(4)
                    .option(ChannelOption.SO_BACKLOG, 128)          // (5)
                    .childOption(ChannelOption.SO_KEEPALIVE, true); // (6)
            // 绑定端口，开始接收进来的连接
            ChannelFuture f = b.bind(port).sync(); // (7)
            f.channel().closeFuture().sync();

        }catch (Exception e){
            e.printStackTrace();
        }finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();

            System.out.println("AcceptFileServer 关闭了");
        }
    }

}
