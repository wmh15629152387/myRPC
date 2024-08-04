package com.example.demo7.netty;

import com.example.demo7.ServiceProvider;
import com.example.demo7.xuliehua.JsonSerializer;
import com.example.demo7.xuliehua.MyDecode;
import com.example.demo7.xuliehua.MyEncode;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * 初始化，主要负责序列化的编码解码， 需要解决netty的粘包问题
 */
@AllArgsConstructor
@NoArgsConstructor
public class NettyClientInitializer extends ChannelInitializer<SocketChannel> {
    private ServiceProvider serviceProvider;
    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
//        // 消息格式 [长度][消息体], 解决粘包问题
//        pipeline.addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE,0,4,0,4));
//        // 计算当前待发送消息的长度，写入到前4个字节中
//        pipeline.addLast(new LengthFieldPrepender(4));

        // 使用自定义的编解码器
        pipeline.addLast(new MyDecode());
        // 编码需要传入序列化器，这里是json，还支持ObjectSerializer，也可以自己实现其他的
        pipeline.addLast(new MyEncode(new JsonSerializer()));

//        pipeline.addLast(new NettyRPCServerHandler(serviceProvider));
        pipeline.addLast(new NettyClientHandler());
    }
}