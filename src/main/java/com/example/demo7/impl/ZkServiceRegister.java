package com.example.demo7.impl;

import com.example.demo7.inter.ServiceRegister;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.ExistsBuilder;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;

public class ZkServiceRegister implements ServiceRegister {
    // curator 提供的zookeeper客户端
    private CuratorFramework client;
    private PathChildrenCache cache;
    // zookeeper根路径节点
    private static final String ROOT_PATH = "MyRPC";

    private static RoundLoadBalance roundLoadBalance = new RoundLoadBalance();

    private static HashMap<String, List<String>> serviceMap  = new HashMap<>();

    // 这里负责zookeeper客户端的初始化，并与zookeeper服务端建立连接
    public ZkServiceRegister(){
        // 指数时间重试
        RetryPolicy policy = new ExponentialBackoffRetry(1000, 3);
        // zookeeper的地址固定，不管是服务提供者还是，消费者都要与之建立连接
        // sessionTimeoutMs 与 zoo.cfg中的tickTime 有关系，
        // zk还会根据minSessionTimeout与maxSessionTimeout两个参数重新调整最后的超时值。默认分别为tickTime 的2倍和20倍
        // 使用心跳监听状态
        this.client = CuratorFrameworkFactory.builder().connectString("127.0.0.1:2181")
                .sessionTimeoutMs(1000).retryPolicy(policy).namespace(ROOT_PATH).build();
        this.client.start();
        System.out.println("zookeeper 连接成功");

//        cache = new PathChildrenCache(client, "/com.example.demo7.inter.BlogService", true);
//        try {
//            cache.start();
//            cache.getListenable().addListener(new PathChildrenCacheListener() {
//                @Override
//                public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
//                    if (event.getType() == PathChildrenCacheEvent.Type.CHILD_UPDATED || event.getType() == PathChildrenCacheEvent.Type.CHILD_REMOVED) {
//                        String[] split = event.getData().getPath().split("/");
//                        serviceMap.remove(split[1]);
//                    }
//
//                }
//            });
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    @Override
    public  void createWatch(String path) {
        path = "/" + path;
        cache = new PathChildrenCache(client, path, true);
        try {
            cache.start();
            cache.getListenable().addListener(new PathChildrenCacheListener() {
                @Override
                public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
                    if (event.getType() == PathChildrenCacheEvent.Type.CHILD_UPDATED || event.getType() == PathChildrenCacheEvent.Type.CHILD_REMOVED) {
                        String[] split = event.getData().getPath().split("/");
                        List<String> strings = client.getChildren().forPath("/" + split[1]);
                        serviceMap.put(split[1], strings);
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void register(String serviceName, InetSocketAddress serverAddress){
        try {
            ExistsBuilder existsBuilder = client.checkExists();
            Stat stat = client.checkExists().forPath("/" + serviceName);
            // serviceName创建成永久节点，服务提供者下线时，不删服务名，只删地址
            if(client.checkExists().forPath("/" + serviceName) == null){
               client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath("/" + serviceName);
            }
            // 路径地址，一个/代表一个节点
            String path = "/" + serviceName +"/"+ getServiceAddress(serverAddress);
            // 临时节点，服务器下线就删除节点
            client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(path);
        } catch (Exception e) {
            System.out.println("此服务已存在");
        }
    }
    // 根据服务名返回地址
    @Override
    public InetSocketAddress serviceDiscovery(String serviceName) {
        List<String> strings = serviceMap.getOrDefault(serviceName, null);
        if(strings != null) {
            String balance = roundLoadBalance.balance(strings);
            return parseAddress(balance);
        }

        try {
            strings = client.getChildren().forPath("/" + serviceName);
            serviceMap.put(serviceName, strings);
            String balance =  roundLoadBalance.balance(strings);
            // 使用轮询的方式
            return parseAddress(balance);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }





    // 地址 -> XXX.XXX.XXX.XXX:port 字符串
    private String getServiceAddress(InetSocketAddress serverAddress) {
        return serverAddress.getHostName() +
                ":" +
                serverAddress.getPort();
    }
    // 字符串解析为地址
    private InetSocketAddress parseAddress(String address) {
        String[] result = address.split(":");
        return new InetSocketAddress(result[0], Integer.parseInt(result[1]));
    }
}