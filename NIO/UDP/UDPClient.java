package com.company.NIO.UDP;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.util.CharsetUtil;

import java.net.InetSocketAddress;
import java.util.List;

public class UDPClient {
    private  EventLoopGroup group;
    private  Bootstrap bootstrap;
    private static int UDPPort=54321;
    public UDPClient(InetSocketAddress remoteAddress) {
        group = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        //引导该 NioDatagramChannel（无连接的）
        bootstrap.group(group).channel(NioDatagramChannel.class)
                //设置 SO_BROADCAST 套接字选项
                .option(ChannelOption.SO_BROADCAST,true)
                .handler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel channel) throws Exception {
                        ChannelPipeline pipeline = channel.pipeline();
                        pipeline.addLast("encoder",new UDPCHandlerS(remoteAddress));
                        pipeline.addLast("handler",new UDPCHandlerR());
                    }
                });
    }
    public Channel getChannel() throws Exception {
        //绑定 Channel
        Channel ch = bootstrap.bind(0).sync().channel();
        return ch;
    }
    public void stop() {
        group.shutdownGracefully();
    }
}
@ChannelHandler.Sharable
class UDPCHandlerS extends MessageToMessageEncoder<VoiceMsg> {
    private  InetSocketAddress remoteAddress;
    //LogEventEncoder 创建了即将被发送到指定的 InetSocketAddress
    // 的 DatagramPacket 消息
    public UDPCHandlerS(InetSocketAddress remoteAddress) {
        this.remoteAddress = remoteAddress;
    }
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext,
                          VoiceMsg logMsg, List<Object> out) throws Exception {
        ByteBuf msg = logMsg.getMsg();
        byte[] header = logMsg.getMsgHeader().getBytes(CharsetUtil.UTF_8);
        //容量的计算：两个long型+消息的内容+分割符
        ByteBuf buf = channelHandlerContext.alloc().buffer(22 + msg.readableBytes());
//        System.out.println(msg.readableBytes());
        buf.writeBytes(header);
        buf.writeBytes(msg);
//        System.out.println(buf.toString(CharsetUtil.UTF_8));
        //将一个拥有数据和目的地地址的新 DatagramPacket 添加到出站的消息列表中
        out.add(new DatagramPacket(buf, remoteAddress));
    }
}
class UDPCHandlerR extends SimpleChannelInboundHandler<VoiceMsg>{
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, VoiceMsg voiceMsg) throws Exception {
        System.out.println(voiceMsg.getMsg().toString());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println(msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
    }
}
