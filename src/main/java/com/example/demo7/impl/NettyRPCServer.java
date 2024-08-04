package com.example.demo7.impl;

import com.example.demo7.netty.NettyServerInitializer;
import com.example.demo7.ServiceProvider;
import com.example.demo7.inter.RPCServer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.AllArgsConstructor;

/**
 * 实现RPCServer接口，负责监听与发送数据
 */
@AllArgsConstructor
public class NettyRPCServer implements RPCServer {
    private ServiceProvider serviceProvider;

    @Override
    public void start(int port) {
        // Netty 服务线程组，bossGroup负责建立连接，workGroup负责具体的请求处理
        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        NioEventLoopGroup workGroup = new NioEventLoopGroup();
        System.out.println("Netty服务端启动了...");
        try {
            // 启动Netty服务器
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            // 初始化ServerBootstrap
            serverBootstrap.group(bossGroup, workGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new NettyServerInitializer(serviceProvider));

            // 绑定端口，同步阻塞等待服务器启动
            ChannelFuture channelFuture = serverBootstrap.bind(port).sync();
            // 阻塞等待服务器关闭
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            // 优雅地关闭线程组
            bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
        }
    }

    @Override
    public void stop() {
        // 这里可以实现停止服务器的逻辑，例如关闭相关资源
    }
}
