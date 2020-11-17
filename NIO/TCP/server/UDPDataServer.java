package com.company.NIO.TCP.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.GlobalEventExecutor;

public class UDPDataServer extends Thread{
    public static ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    private int Int;
    UDPDataServer(int Int){
        this.Int=Int;
    }
    @Override
    public void run() {
        EventLoopGroup bossGroup = new NioEventLoopGroup(); // (1)
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap(); // (2)
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class) // (3)
                    .childHandler(new UDPDataServerHandlerAdapter()).option(ChannelOption.SO_BACKLOG, 128) // 设置的ServerChannel的一些选项
                    .childOption(ChannelOption.SO_KEEPALIVE, true); // 设置的ServerChannel的子Channel的选项
            System.out.println("UDPDataServer 启动了");
            // 绑定端口，开始接收进来的连接
            ChannelFuture f = b.bind(Int).sync(); // (7)
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
}
@ChannelHandler.Sharable
class  UDPDataServerHandlerAdapter extends ChannelInboundHandlerAdapter{
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        UDPDataServer.channels.add(ctx.channel());
    }
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}