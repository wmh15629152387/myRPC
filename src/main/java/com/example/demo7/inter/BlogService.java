package com.example.demo7.inter;

import com.example.demo7.pojo.Blog;

// 新的服务接口
public interface BlogService {
    Blog getBlogById(Integer id);
}