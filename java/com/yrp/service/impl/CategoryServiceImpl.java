package com.yrp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yrp.common.Code;
import com.yrp.dao.CategoryDao;
import com.yrp.exception.BusinessException;
import com.yrp.pojo.Category;
import com.yrp.pojo.Dish;
import com.yrp.pojo.Setmeal;
import com.yrp.service.CategoryService;
import com.yrp.service.DishService;
import com.yrp.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Auther: 公众号/B站：是叶十三
 * @Date: 2022/7/15
 * @Description: com.yrp.service.impl
 * @version: 1.0
 */
@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, Category> implements CategoryService {
    @Autowired
    private DishService dishServiceImpl;

    @Autowired
    private SetmealService setmealServiceImpl;

    @Autowired
    private CategoryDao categoryDaoImplImpl;


    @Autowired
    private CategoryDao categoryDaoImpl;

    @Override
    public void deleteById(Long id) {
        //1、先去 菜品表 和 套套餐 中查询是否存在某个套餐或者菜品属于当前需要删除的分类
        //    1.1 如果存在属于该分类，则抛出业务层异常，给出相应的提示信息
        LambdaQueryWrapper<Dish> lqw = new LambdaQueryWrapper<>();
        lqw.eq(Dish::getCategoryId, id);
        int count1 = dishServiceImpl.count(lqw);
        if (count1 > 0) {
            throw new BusinessException(Code.BUSINESS_ERR, "该分类已经有相关菜品，请先删除相关菜品才能删除该分类哦！");
        }
        LambdaQueryWrapper<Setmeal> lqw2 = new LambdaQueryWrapper<>();
        lqw2.eq(Setmeal::getCategoryId, id);
        int count2 = setmealServiceImpl.count(lqw2);
        if (count2 > 0) {
            throw new BusinessException(Code.BUSINESS_ERR, "该分类已经存在套餐，请先删除相关套餐才能删除该分类哦！");
        }
        //   1.2 如果不存属于该分类，则可以进行删除
        int index = categoryDaoImplImpl.deleteById(id);
        //     1.2.1 如果删除失败，则抛出业务层异常
        if (index <= 0) {
            throw new BusinessException(Code.BUSINESS_ERR, "删除分类失败，请稍后重试!");
        }
    }

    @Override
    public List<Category> selList(Category category) {
        LambdaQueryWrapper<Category> lqw = new LambdaQueryWrapper<>();
        // 根据type属性，判断是查询套餐，还是查询单一的套餐，如果type属性为空，则全部查出来
        lqw.eq(category.getType() != null, Category::getType, category.getType());
        // 查询正在售卖的商品或者套餐
        // 根据sort升序排序
        // 根据更新时间降序
        lqw.orderByAsc(Category::getSort).orderByDesc(Category::getUpdateTime);

        List<Category> categoryList = categoryDaoImpl.selectList(lqw);
        return categoryList;
    }
}
