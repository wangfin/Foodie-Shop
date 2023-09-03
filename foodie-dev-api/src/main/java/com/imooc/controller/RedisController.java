package com.imooc.controller;

import com.imooc.utils.RedisUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@ApiIgnore
@RestController
@RequestMapping("redis")
public class RedisController {

    @Autowired
    private RedisUtils redisUtils;

    @GetMapping("/set")
    public Object set(String key, String value){
        redisUtils.set(key, value);
        return "OK~";
    }

    @GetMapping("/get")
    public String get(String key){
        return redisUtils.get(key);
    }

    @GetMapping("/delete")
    public Object hello(String key){
        redisUtils.delete(key);
        return "OK~";
    }

    /**
     * 批量查询 mget
     * @param keys
     * @return
     */
    @GetMapping("/mget")
    public Object mget(String... keys) {

        List<String> keyList = Arrays.asList(keys);
        return redisUtils.multiGet(keyList);
    }

    /**
     * 批量查询 管道pipline
     * pipline查询出来的类型更多样
     * @param keys
     * @return
     */
    @GetMapping("/batchGet")
    public Object batchGet(String... keys) {

        List<String> keyList = Arrays.asList(keys);
        return redisUtils.batchget(keyList);
    }
}
