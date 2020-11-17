package com.company.NIO.TCP.server;


import com.company.NIO.UDP.UDPServer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.net.InetSocketAddress;
import java.util.HashMap;

/**
 *服务端TCP端口
 * 12345-ServerAcceptBase64
 * 11111-ServerSendVoice
 *
  */
public class Server extends Thread{
    static {
        new AcceptFileS(12345).start();
        new UDPServer(new InetSocketAddress(11152)).start();
        new UDPDataServer(11111).start();
    }
    public  static HashMap<String,String> mapName=new HashMap<>();
    private int port;
    public Server(int port)
    {
        this.port=port;
    }
    public void run()
    {

        EventLoopGroup bossGroup = new NioEventLoopGroup(); // (1)
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap(); // (2)
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class) // (3)
                    .childHandler(new ServerInitializer())  //(4)
                    .option(ChannelOption.SO_BACKLOG, 128)          // (5)
                    .childOption(ChannelOption.SO_KEEPALIVE, true); // (6)
            System.out.println("SimpleChatServer 启动了");
            // 绑定端口，开始接收进来的连接
            ChannelFuture f = b.bind(port).sync(); // (7)
            // 等待服务器  socket 关闭 。
            // 在这个例子中，这不会发生，但你可以优雅地关闭你的服务器。
            f.channel().closeFuture().sync();

        }catch (Exception e){
            e.printStackTrace();
        }finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
            System.out.println("SimpleChatServer 关闭了");
        }
    }

//    public static void main(String [] args)throws Exception
//    {
//        int port = Integer.parseInt(args[0]);
//        System.out.println(port);
//            Thread t = new Server(port);
//            t.run();
//    }
}

