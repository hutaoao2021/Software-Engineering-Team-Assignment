package com.yrp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yrp.pojo.Category;

import java.util.List;

/**
 * @Auther: 公众号/B站：是叶十三
 * @Date: 2022/7/14
 * @Description: com.yrp.service
 *  菜品+套餐的分类-业务层接口
 * @version: 1.0
 */

public interface CategoryService extends IService<Category> {
     /**
      * 根据id删除分类
      * @param id
      */
     void deleteById(Long id);

     /**
      * type = 0表示，没有使用type作文条件查询
      * 根据指定条件 - 查询分类列表
      */
     List<Category> selList(Category category);
}
