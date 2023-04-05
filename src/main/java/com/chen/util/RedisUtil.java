package com.chen.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class RedisUtil {

    @Autowired
    private RedisTemplate redisTemplate;

    public void set(String key,Object value){
        redisTemplate.opsForValue().set(key,value);
    }

    public void set(String key, Object value, long timeout, TimeUnit timeUnit){
        redisTemplate.opsForValue().set(key,value,timeout,timeUnit);
    }

    public boolean setIfAbsent(String key,Object value,long timeout,TimeUnit timeUnit){
        return redisTemplate.opsForValue().setIfAbsent(key,value,timeout,timeUnit);
    }

    public <T> T get(String key,Class<?> T){
        return (T)redisTemplate.opsForValue().get(key);
    }

    public boolean expire(String key,long timeout,TimeUnit timeUnit){
        return redisTemplate.expire(key,timeout,timeUnit);
    }

}
