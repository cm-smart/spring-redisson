package com.chen.dao;


import com.chen.pojo.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ProductDao {

    public long insert(Product product);

    public int update(@Param("item") Product product);

    public Product get(@Param("productId") Long productId);
}
