package com.chen.service.impl;

import com.alibaba.fastjson.JSON;
import com.chen.common.RedisKeyPrefixConst;
import com.chen.dao.ProductDao;
import com.chen.pojo.Product;
import com.chen.service.ProductService;
import com.chen.util.RedisUtil;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class ProductServiceImpl implements ProductService {

    public static final Integer PRODUCT_CACHE_TIMEOUT = 60*60*24;
    public static final String EMPTY_CACHE = "{}";
    public static final String LOCK_PRODUCT_HOT_CACHE_CREATE_PREFIX = "lock:product:hot_cache_create:";
    public static final String LOCK_PRODUCT_HOT_CACHE_UPDATE_PREFIX = "lock:product:hot_cache_update:";

    @Autowired
    private ProductDao productDao;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private RedisUtil redisUtil;

    @Transactional
    @Override
    public Product create(Product product) {

        productDao.insert(product);//插入返回的id到product中了
        redisUtil.set(RedisKeyPrefixConst.PRODUCT_CACHE + product.getId(), JSON.toJSONString(product),
                genProductCacheTimeout(), TimeUnit.SECONDS);

        return product;
    }

    @Override
    public Product update(Product product) {

        RReadWriteLock readWriteLock = redissonClient.getReadWriteLock(LOCK_PRODUCT_HOT_CACHE_UPDATE_PREFIX + product.getId());
        RLock rLock = readWriteLock.writeLock();
        rLock.lock();

        try{
            productDao.update(product);
            redisUtil.set(RedisKeyPrefixConst.PRODUCT_CACHE + product.getId(),JSON.toJSONString(product),
                    genProductCacheTimeout(),TimeUnit.SECONDS);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            rLock.unlock();
        }


        return product;
    }

    /**
     * 1.缓存击穿
     *      1.针对某个访问非常频繁的热点数据的请求，无法在缓存中进行处理，紧接着，访问该数据的大量请求，一下子都发送到了后端数据库，导致了数据库压力激增，会影响数据库处理其他请求
     * 2.缓存穿透
     *      1.是用户访问的数据既不在缓存当中，也不在数据库中
     *      2.解决方法：缓存空值，布隆过滤器
     * 3.缓存雪崩
     *      1.在使用缓存时，通常会对缓存设置过期时间，一方面目的是保持缓存与数据库数据的一致性，另一方面是减少冷缓存占用过多的内存空间
     *      但当缓存中大量热点缓存采用了相同的实效时间，就会导致缓存在某一个时刻同时实效，请求全部转发到数据库，从而导致数据库压力骤增，甚至宕机。从而形成一系列的连锁反应，造成系统崩溃等情况
     *      2.解决方法：设置随机过期时间
     * 4.缓存与数据库双写不一致
     *
     * @param productId
     * @return
     */
    @Override
    public Product get(Long productId) {
        Product product = null;

        String productCacheKey = RedisKeyPrefixConst.PRODUCT_CACHE + productId;

        product = getProductFromCache(productCacheKey);
        if(product != null){
            return product;
        }

        //锁的粒度：给商品添加锁，不能全局添加锁
        //解决方法：redis分布式锁，根据商品id设置锁
        //DCL
        RLock rLock = redissonClient.getLock(LOCK_PRODUCT_HOT_CACHE_CREATE_PREFIX + productId);
        rLock.lock();
        try{
            product = getProductFromCache(productCacheKey);
            if(product != null){
                return product;
            }

            //解决缓存与数据库双写不一致问题,和修改相对应设置一个分布式读写锁
            RReadWriteLock readWriteLock = redissonClient.getReadWriteLock(LOCK_PRODUCT_HOT_CACHE_UPDATE_PREFIX + productId);
            RLock readLock = readWriteLock.readLock();
            readLock.lock();
            try{
                product = productDao.get(productId);
                if(product != null){
                    redisUtil.set(productCacheKey,JSON.toJSONString(product),genProductCacheTimeout(),TimeUnit.SECONDS);
                }else{
                    redisUtil.set(productCacheKey,EMPTY_CACHE);
                }
            }finally {
                readLock.unlock();
            }

        }catch (Exception e){
            e.printStackTrace();
        }finally {
            rLock.unlock();
        }

        return product;
        //JVM锁，并且锁全局性能不行
//        synchronized (this){
//            product = getProductFromCache(productCacheKey);
//            if(product != null){
//                return product;
//            }
//
//            product = productDao.get(productId);
//            if(product != null){
//                redisUtil.set(productCacheKey,JSON.toJSONString(product),genProductCacheTimeout(),TimeUnit.SECONDS);
//            }else{
//                redisUtil.set(productCacheKey,EMPTY_CACHE);
//            }
//
//            return product;
//        }

    }

    private Integer genProductCacheTimeout(){
        return PRODUCT_CACHE_TIMEOUT * new Random() .nextInt(5) * 60 * 60;
    }

    private Product getProductFromCache(String productCacheKey){
        Product product = null;

        String productStr = redisUtil.get(productCacheKey,String.class);
        if(!StringUtils.isEmpty(productStr)){

            if(EMPTY_CACHE.equals(productStr)){
                return new Product();
            }
            product = JSON.parseObject(productStr,Product.class);
            //超时时间延长
            redisUtil.expire(productCacheKey,genProductCacheTimeout(),TimeUnit.SECONDS);
        }

        return product;
    }
}
