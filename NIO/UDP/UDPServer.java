package com.company.NIO.UDP;

import com.company.NIO.Server;
import com.company.NIO.TCP.server.ServerHandler;
import com.company.NIO.TCP.server.UDPDataServer;
import com.company.Utils.NameUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.net.InetSocketAddress;
import java.util.List;

public class UDPServer extends Thread {
    private  EventLoopGroup group;
    private  Bootstrap bootstrap;
    private final static int UDPPort = 54321;
    private InetSocketAddress address;
    public UDPServer(InetSocketAddress address) {
        this.address=address;
    }

    @Override
    public void run() {
        try {
            group = new NioEventLoopGroup();
            bootstrap = new Bootstrap();
            //引导该 NioDatagramChannel
            bootstrap.group(group)
                    .channel(NioDatagramChannel.class)
                    //设置套接字选项 SO_BROADCAST
                    .option(ChannelOption.SO_BROADCAST, true)
                    //允许端口重用，可开启多个接收方
                    .option(ChannelOption.SO_REUSEADDR, true)
                    .handler(new ChannelInitializer<Channel>() {
                        @Override
                        protected void initChannel(Channel channel) throws Exception {
                            ChannelPipeline pipeline = channel.pipeline();
                            pipeline.addLast("decoder",new UDPSDecoder());
                            pipeline.addLast("handler",new UDPSHandler());
                        }
                    })
                    .localAddress(address);

            System.out.println("UDPServer开启");
            bind().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        finally {
            Stop();
        }
    }

    public Channel bind() {
        //绑定 Channel。注意，DatagramChannel 是无连接的
        return bootstrap.bind().syncUninterruptibly().channel();
    }

    public void Stop() {
        group.shutdownGracefully();
    }
//        public static void main(String[] args) throws Exception {
//        //构造一个新的 UdpAnswerSide并指明监听端口
//       new UDPServer(new InetSocketAddress(UDPPort));
//    }


    class UDPSHandler  extends SimpleChannelInboundHandler<VoiceMsg> {
        @Override
        public void exceptionCaught(ChannelHandlerContext ctx,
                                    Throwable cause)  {
            //当异常发生时，打印栈跟踪信息，并关闭对应的 Channel

            cause.printStackTrace();
            ctx.close();
        }

        @Override
        public void channelRead0(ChannelHandlerContext ctx,
                                 VoiceMsg event) throws Exception {
                ServerHandler.WhoSpeak(NameUtil.get_UDP_Ip(event.getMsgHeader()));
//            ServerHandler.SendAllVoice(NameUtil.get_UDP_Ip(event.getMsgHeader()),);
//            System.out.println("channelRead0"+event.getMsg().toString(CharsetUtil.UTF_8));
            for(Channel channel:UDPDataServer.channels){
                if(Server.TestVoice){
                    channel.writeAndFlush(Unpooled.copiedBuffer(event.getMsg()));
                }else{
                    if(!channel.remoteAddress().toString().split(":")[0].equals( ctx.channel().remoteAddress().toString().split(":")[0]))
                    channel.writeAndFlush(Unpooled.copiedBuffer(event.getMsg()));
                }


            }
        }
    }
    class UDPSDecoder extends MessageToMessageDecoder<DatagramPacket> {
        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            cause.printStackTrace();
        }

        @Override
        protected void decode(ChannelHandlerContext ctx,
                              DatagramPacket datagramPacket, List<Object> out) {
            //获取对 DatagramPacket 中的数据（Bytebuf）的引用
            ByteBuf data = datagramPacket.content();
            //获得发送时间
            byte[] header =new  byte[22];
            data.readBytes(header);
//            System.out.println(new String(header));
            //获取读索引的当前位置，就是分隔符的索引+1
            //提取日志消息，从读索引开始，到最后为日志的信息
            ByteBuf sendMsg = Unpooled.copiedBuffer(data.slice(data.readerIndex(),data.readableBytes()));
            VoiceMsg event = new VoiceMsg(new String(header), sendMsg);
            //作为本handler的处理结果，交给后面的handler进行处理
            out.add(event);
        }
    }
}
