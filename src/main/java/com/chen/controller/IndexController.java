package com.chen.controller;

import jodd.util.StringUtil;
import org.redisson.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class IndexController {

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    @Value("${server.port}")
    private int port;


    /**
     * 库存分布式锁扣减
     * @return
     */
    @RequestMapping("/deduct_stock")
    public String deductStock(){
        String lockKey = "lock:product:101";
        RLock redissonLock = redissonClient.getLock(lockKey);
        RReadWriteLock readWriteLock = redissonClient.getReadWriteLock(lockKey);

        //加锁
        //性能优化：1.给每个商品加锁，优化锁的粒度,假设有200个商品，分成10组，每一组对应一个key；2.读多写少，用读写锁
        redissonLock.lock();
        try{
            String stock_Str = redisTemplate.opsForValue().get("stock:product:101");
            if(!StringUtil.isEmpty(stock_Str)){
                int stock = Integer.parseInt(stock_Str);
                if(stock > 0){
                    int realStock = stock - 1;
                    redisTemplate.opsForValue().set("stock:product:101",realStock+"");
                    System.out.println("扣减成功，剩余库存：" + realStock);
                }else{
                    System.out.println("扣减失败，库存不足");
                }

            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            //解锁
            redissonLock.unlock();
        }

        return "success " + port;
    }
}
