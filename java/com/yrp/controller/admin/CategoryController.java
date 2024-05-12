package com.yrp.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yrp.common.Code;
import com.yrp.common.R;
import com.yrp.exception.BusinessException;
import com.yrp.exception.SystemException;
import com.yrp.pojo.Category;
import com.yrp.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Auther: 公众号/B站：是叶十三
 * @Date: 2022/7/16
 * @Description: com.yrp.controller
 * @version: 1.0
 */

/**
 * 菜系(category)与 菜品（dish）与套餐（setmeal）这两张表有关联
 */
// http://localhost/category/page?page=1&pageSize=10
@RestController
@RequestMapping("/category")
@Slf4j
public class CategoryController {

    @Autowired
    private CategoryService categoryServiceImpl;

    /**
     * 分页查询分类
     * @param page
     * @param pageSize
     * @return
     */
    //  http://localhost/category/page?page=1&pageSize=10
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize){
        // 创建一个用于存在分页参数的对象page
        Page<Category> p = new Page<>();
        // 设置页码，每页显示的条数
        p.setCurrent(page);
        p.setSize(pageSize);
        //构造条件构造器
        LambdaQueryWrapper<Category> lqw = new LambdaQueryWrapper();
        //添加排序条件（按sort升序排序）
        lqw.orderByAsc(Category::getSort);
        // 指定查询
        Page<Category> list = categoryServiceImpl.page(p, lqw);
        log.info("list={}",list);
        return R.success(p);
    }
    /**
     * 新增分类（套餐+菜品）
     * @param category
     * @return
     */
    @PostMapping()
    public R<String> save(@RequestBody Category category){
        log.info("category={}",category);
        //在数据库中查询是否存在该套餐(根据套餐名称)
        //  如若存在，则抛出一个业务层异常，
        //  如果不存在，则直接插入即可
        //      插入成功
        //      插入失败

        //在数据库中查询是否存在该套餐(根据套餐名称)
        String cname = category.getName();
        LambdaQueryWrapper<Category> lqw = new LambdaQueryWrapper();
        lqw.eq(Category::getName,cname);
        Category one = categoryServiceImpl.getOne(lqw);
        //  如若存在，则抛出一个业务层异常，
        if (one != null){
            throw new BusinessException(Code.BUSINESS_ERR,"该分类已存在！");
        }
        // 如果不存在，则直接插入即可
        boolean save = categoryServiceImpl.save(category);
        if (save == false){
            throw new SystemException(Code.BUSINESS_ERR,"插入失败，请稍后重试！");
        }
        return R.success("新增分类成功");
    }
    /**
     * 修改分类
     * @param category
     * @return
     */
    @PutMapping()
    public R<String> update(@RequestBody Category category){
        log.info("category={}",category);
        /**
         * 根据id去查询该分类是否存在
         *              如果不存在该分类，抛出业务层异常，让全局异常处理器进行捕获
         *              如果存该分类，才能进行修改
         *
         *                   如果修改成功，返回成功提示信息“修改分类成功”
          */
        // 根据id去查询该分类是否存在
        LambdaQueryWrapper<Category> lqw = new LambdaQueryWrapper();
        lqw.eq(Category::getId,category.getId());
        Category one = categoryServiceImpl.getOne(lqw);
        // 如果不存在该分类，抛出业务层异常，让全局异常处理器进行捕获
        if (one == null ){
            throw new BusinessException(Code.BUSINESS_ERR,"修改失败，该分类已经不存在！");
        }
        // 如果存该分类，才能进行修改
        boolean b = categoryServiceImpl.updateById(category);
        //  如果修改失败，继续抛出业务层异常，提示修改失败，请稍后重试
        if (b == false){
            throw new BusinessException(Code.SYSTEM_ERR,"修改失败，请稍后重试！");
        }
        //  如果修改成功，返回成功提示信息“修改分类成功”
        return R.success("修改分类成功！");
    }
    // http://localhost/category?id=1548149155753459714

    /**
     * 根据id删除菜系(category)
     * @param id
     * @return
     */
    @DeleteMapping("/{id}")
    public R<String> delete(@PathVariable Long id){
        /**
         思路：
         1、先去 菜品表 和 套套餐 中查询是否存在某个套餐或者菜品属于当前需要删除的分类
            1.1 如果存在属于该分类，则抛出业务层异常，给出相应的提示信息
            1.2 如果不存属于该分类，则可以进行删除
         */
        log.info("id={}",id);
        categoryServiceImpl.deleteById(id);
        return R.success("删除分类成功！");
    }
    // http://localhost/category/list?type=1

    /**
     * 显示菜品列表
     * category 中封装了参数type
      */
    @GetMapping("/list")
    public R<List<Category>> showList(Category category){
        Integer type = category.getType();
        log.info("category = {}",category);
        List<Category> categoryList = categoryServiceImpl.selList(category);
        return R.success(categoryList);
    }
}
