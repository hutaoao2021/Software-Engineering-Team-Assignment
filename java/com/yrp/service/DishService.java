package com.yrp.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yrp.dto.DishDto;
import com.yrp.pojo.Dish;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @Auther: 公众号/B站：是叶十三
 * @Date: 2022/7/16
 * @Description: com.yrp.service
 * @version: 1.0
 */


public interface DishService extends IService<Dish> {

    void save(DishDto dishDto);

    /**
     * 分页查询（设计到 dish表 category表）
     * @param currentPage
     * @param pageSize
     * @param name
     * @return
     */
    Page<DishDto> selPage(int currentPage, int pageSize, String name);

    /**
     * 根据id查询菜品信息以及口味信息
     * @param id
     * @return
     */

    DishDto selById(Long id);


    /**
     * 修改菜品
     * @param DishDto
     */
    @Transactional
    void update(DishDto DishDto);

    /**
     * 根据id删除dish、以及它说关联的flavor
     * @param ids
     */
    @Transactional
    void deleteById(Long[] ids);

    void updById(Integer status, Long[] ids);

    List<DishDto> showListByCategory(Long categoryId);
}
