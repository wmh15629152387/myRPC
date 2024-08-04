package com.example.demo7;

import com.example.demo7.impl.BlogServiceImpl;
import com.example.demo7.impl.NettyRPCServer;
import com.example.demo7.impl.UserServiceImpl;
import com.example.demo7.inter.RPCServer;

public class TestServer2 {
    public static void main(String[] args) {
        ServiceProvider serviceProvider = new ServiceProvider("127.0.0.1", 8901);
        serviceProvider.provideServiceInterface(new UserServiceImpl());
        serviceProvider.provideServiceInterface(new BlogServiceImpl());

        RPCServer RPCServer = new NettyRPCServer(serviceProvider);
        RPCServer.start(8901);
    }
}
