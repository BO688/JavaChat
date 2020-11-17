package com.company.NIO.TCP.client;


import com.company.NIO.Client;
import com.company.Utils.AudioUtil;
import com.company.Utils.Base64Util;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import static com.company.NIO.TCP.client.Client.sendFileSuffix;

public class UpdateFile extends Thread{
    private String path;
    private String iport;
    UpdateFile(String path,String iport){
        this.path=path;
        this.iport=iport;
    }

    @Override
    public void run() {
        System.out.println("UpdateFile");
        // 配置客户端
        NioEventLoopGroup group = new NioEventLoopGroup();
        Bootstrap b = new Bootstrap();
        b.group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .handler(new ClientInitializer(this.getClass()));
        ChannelFuture f ;
        String base64;
        try {
            base64= Base64Util.encodeBase64File(path).getBase64();
            f = b.connect(Client.Host, 12345).sync();
        Channel channel = f.channel();
        if(path.endsWith(".mp3")){
            com.company.NIO.TCP.client.Client.channel.writeAndFlush("filename:"+(path.substring(path.lastIndexOf("."))+"|||||"+ (int) AudioUtil.GetAudioTime(path))+"OVER\n");
        }else{
            sendFileSuffix(path);
        }
        Thread.sleep(100);
            channel.writeAndFlush(iport+"&&&&&"+base64+"OVER\n");
            Thread.sleep(100);
        group.shutdownGracefully();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
