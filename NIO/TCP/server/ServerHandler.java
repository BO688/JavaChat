package com.company.NIO.TCP.server;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

public class ServerHandler extends SimpleChannelInboundHandler<String> { // (1)
    public static void SendAll(ChannelHandlerContext ctx, String msg){
        Channel incoming = ctx.channel();
        for (Channel channel : channels) {
            channel.writeAndFlush("["+ Server.mapName.get(incoming.remoteAddress().toString())+"]"+ msg + "OVER\n");
        }
    }
    public static void WhoSpeak(String Name){
        for (Channel channel : channels) {
            channel.writeAndFlush("speaking"+ Name+"OVER\n");
        }
    }
    public static void SendAllPic(String From, String msg){
        for (Channel channel : channels) {
            channel.writeAndFlush("From ["+ Server.mapName.get(From)+"] start:"+ msg +"OVER\n");
        }
    }
    private  String s="";
    private boolean check=false;
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//        System.err.println(msg.toString());
        if(msg.toString().length()>4){
       if(msg.toString().endsWith("OVER")){
            check=true;
           s=s+msg.toString().substring(0,msg.toString().length()-4);
        }else{
           s=s+msg.toString();
       }
        }else{
            s=s+msg.toString();
            s=s.substring(0, s.length()-4);
            check=true;
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
//        System.err.println("complete"+s);
        if(!check){return;}
            if(s.contains("i'm ")){
                s=s.replace("i'm ","");
                if(Server.mapName.containsValue(s)){
                    ctx.channel().writeAndFlush("name errorOVER\n");
                }else{
                    ctx.channel().writeAndFlush("welcomeOVER\n");
                    Server.mapName.put(ctx.channel().remoteAddress().toString(),s);
                    channels.writeAndFlush("[SERVER] - " + s + " 加入OVER\n");
                    channels.add(ctx.channel());
                }
            }else if("who am i".equals(s)){
                ctx.channel().writeAndFlush("Host-port:"+ctx.channel().remoteAddress().toString()+"OVER\n");
            }
            else{
                SendAll(ctx,s);
            }
            s="";
            check=false;
    }

    /**
     * A thread-safe Set  Using ChannelGroup, you can categorize Channels into a meaningful group.
     * A closed Channel is automatically removed from the collection,
     */
    public static ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {  // (2)

    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {  // (3)
        Channel incoming = ctx.channel();
        // Broadcast a message to multiple Channels
        channels.writeAndFlush("[SERVER] - " + Server.mapName.get(incoming.remoteAddress().toString()) + " 离开OVER\n");
        Server.mapName.remove(incoming.remoteAddress().toString());
        // A closed Channel is automatically removed from ChannelGroup,
        // so there is no need to do "channels.remove(ctx.channel());"
    }
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String s) throws Exception { // (4)
        System.out.println(s);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        Channel incoming = ctx.channel();
        System.out.println("SimpleChatClient:"+incoming.remoteAddress()+"掉线");
        // 当出现异常就关闭连接
        cause.printStackTrace();
        ctx.close();
    }
}
