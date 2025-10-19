package com.air.aiagent.service.impl;

import com.air.aiagent.domain.entity.Product;
import com.air.aiagent.mapper.ProductMapper;
import com.air.aiagent.service.productService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import org.springframework.stereotype.Service;

/**
* @author 30280
* @description 针对表【product(商品信息表)】的数据库操作Service实现
* @createDate 2025-10-19 12:33:57
*/
@Service
public class productServiceImpl extends ServiceImpl<ProductMapper, Product> implements productService {

}




