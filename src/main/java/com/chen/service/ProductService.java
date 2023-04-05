package com.chen.service;

import com.chen.pojo.Product;

public interface ProductService {

    public Product create(Product product);

    public Product update(Product product);

    public Product get(Long productId);
}
