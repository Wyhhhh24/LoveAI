package com.air.aiagent.mapper;
import com.air.aiagent.domain.entity.Product;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;


import java.util.List;


/**
* @author 30280
* @description 针对表【product(商品信息表)】的数据库操作Mapper
* @createDate 2025-10-19 12:33:57
* @Entity generator.domain.product
*/
public interface ProductMapper extends BaseMapper<Product> {

    /**
     * 根据场景查询推荐商品，这里可以对数据库中的 场景 以及 标签 进行模糊匹配，返回匹配到的商品列表
     * @param scene 场景关键词
     * @param limit 限制数量
     * @return 商品列表
     */
    @Select("SELECT * FROM product " +
            "WHERE status = 1 " +
            "AND (scene LIKE CONCAT('%', #{scene}, '%') OR tags LIKE CONCAT('%', #{scene}, '%')) " +
            "LIMIT #{limit}")
    List<Product> selectByScene(@Param("scene") String scene, @Param("limit") int limit);

    /**
     * 根据分类查询热门商品
     * @param category 分类
     * @param limit 限制数量
     * @return 商品列表
     */
    @Select("SELECT * FROM product " +
            "WHERE status = 1 AND category = #{category} " +
            "LIMIT #{limit}")
    List<Product> selectHotByCategory(@Param("category") String category, @Param("limit") int limit);
}




