package com.yrp.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yrp.pojo.Category;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Auther: 公众号/B站：是叶十三
 * @Date: 2022/7/14
 * @Description: com.yrp.dao
 * @version: 1.0
 */

/**
 * 菜品+套餐的分类-数据层
 */
@Mapper
public interface CategoryDao extends BaseMapper<Category> {

}
