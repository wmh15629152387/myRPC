package com.example.demo7;

import com.example.demo7.impl.NettyRPCClient;
import com.example.demo7.inter.BlogService;
import com.example.demo7.inter.UserService;
import com.example.demo7.pojo.Blog;
import com.example.demo7.pojo.User;

public class TestClient {
    public static void main(String[] args) {

        try {
            // 构建一个使用java Socket传输的客户端
            NettyRPCClient nettyRPCClient = new NettyRPCClient();
            // 把这个客户端传入代理客户端
            ClientProxy rpcClientProxy = new ClientProxy(nettyRPCClient);
            // 客户中添加新的测试用例
            BlogService blogService = rpcClientProxy.getProxy(BlogService.class);
            Blog blogById = blogService.getBlogById(10000);
            System.out.println("BlogService第一次从服务端得到的blog为：" + blogById);
            Blog blogById1 = blogService.getBlogById(10000);
            System.out.println("BlogService第二次从服务端得到的blog为：" + blogById1);
            Blog blogById2 = blogService.getBlogById(10000);
            System.out.println("BlogService第三次从服务端得到的blog为：" + blogById2);

            UserService userService = rpcClientProxy.getProxy(UserService.class);
            User userByUserId = userService.getUserByUserId(1);
            System.out.println("userService第一次从服务端得到的User为：" + userByUserId);

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("客户端启动失败");
        }
    }
}