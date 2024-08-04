package com.example.demo7.inter;

import com.example.demo7.pojo.RPCRequest;
import com.example.demo7.pojo.RPCResponse;

// 共性抽取出来
public interface RPCClient {
    RPCResponse sendRequest(RPCRequest response);

    void createWatch(String path);
}