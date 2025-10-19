package com.air.aiagent.service.impl;
import com.air.aiagent.domain.entity.Product;
import com.air.aiagent.domain.vo.ProductRecommendVO;
import com.air.aiagent.mapper.ProductMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 商品推荐服务
 */
@Service
@Slf4j
public class ProductRecommendService {
    
    @Resource
    private ProductMapper productMapper;
    
    /**
     * 根据场景推荐商品
     * 
     * @param scene 场景
     * @param maxCount 最多推荐数量
     * @return 商品列表
     */
    public List<Product> recommendByScene(String scene, int maxCount) {
        // 优先从数据库精确匹配场景
        List<Product> products = productMapper.selectByScene(scene, maxCount);
        
        // 如果没有匹配的，返回热门商品
        if (products.isEmpty()) {
            log.warn("场景 [{}] 没有匹配的商品，返回热门礼物", scene);
            products = productMapper.selectHotByCategory("礼物", maxCount);
        }
        
        log.info("场景 [{}] 推荐商品数量: {}", scene, products.size());
        return products;
    }
    
    /**
     * 根据分类推荐商品
     * 
     * @param category 分类：书籍/礼物
     * @param maxCount 最多推荐数量
     * @return 商品列表
     */
    public List<Product> recommendByCategory(String category, int maxCount) {
        return productMapper.selectHotByCategory(category, maxCount);
    }
    
    /**
     * 转换为VO
     * 
     * @param products 商品列表
     * @return VO列表
     */
    public List<ProductRecommendVO> convertToVO(List<Product> products) {
        return products.stream()
            .map(product -> {
                ProductRecommendVO vo = new ProductRecommendVO();
                BeanUtils.copyProperties(product, vo);
                vo.setProductId(product.getId());
                return vo;
            })
            .collect(Collectors.toList());
    }
    
    /**
     * 根据商品ID批量查询并转换为VO
     * 
     * @param productIds 商品ID列表
     * @return VO列表
     */
    public List<ProductRecommendVO> getProductVOsByIds(List<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<Product> products = productMapper.selectBatchIds(productIds);
        return convertToVO(products);
    }
}