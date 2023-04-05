package com.chen.controller;

import com.chen.pojo.Product;
import com.chen.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class ProductController {

    @Autowired
    private ProductService productService;

    @RequestMapping(value = "/create",method = RequestMethod.POST)
    public Product create(@RequestBody Product product){
        return productService.create(product);
    }

    @RequestMapping(value = "/update",method = RequestMethod.POST)
    public Product update(@RequestBody Product product){
        return productService.update(product);
    }

    @RequestMapping("/get/{productId}")
    public Product getProduct(@PathVariable Long productId){
        return productService.get(productId);
    }
}
