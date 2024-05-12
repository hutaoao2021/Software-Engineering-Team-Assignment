package com.yrp.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yrp.common.R;
import com.yrp.dto.DishDto;
import com.yrp.pojo.Dish;
import com.yrp.service.DishService;
import com.yrp.utils.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @Auther: 公众号/B站：是叶十三
 * @Date: 2022/7/17
 * @Description: com.yrp.controller
 * @version: 1.0
 */
@RestController
@RequestMapping("/dish")
@Slf4j
public class DishController {

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private DishService dishServiceImpl;

    /**
     * 新增菜品
     *
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto) {
        log.info("saving dish dishDto = {}", dishDto);
        System.out.println("描述:" + dishDto.getId());
        Long categoryId = dishDto.getCategoryId();
        Integer status = dishDto.getStatus();// 默认为1
        // 从缓存中清空categoryId所在分类下的菜品
        String key = "dish_" + categoryId + "_" + status;
        redisUtil.del(key);
        dishServiceImpl.save(dishDto);
        return R.success("添加菜品成功！");
    }

    // http://localhost/dish/page?page=1&pageSize=10
    @GetMapping("/page")
    public R<Page<DishDto>> page(int page, int pageSize, String name) {
        // 构建分页工具类
        log.info("page={}, pageSize = {}, name= {}", page, pageSize, name);
        Page<DishDto> dishDtoPage = dishServiceImpl.selPage(page, pageSize, name);
        return R.success(dishDtoPage);
    }

    /**
     * 根据id查询菜品信息（dish） + 口味信息（dish_flavor）
     *
     * @param id
     * @return
     */
    // http://localhost/dish/1548578190358851585
    @GetMapping("/{id}")
    public R<DishDto> selById(@PathVariable Long id) {
        log.info("id = {}", id);
        DishDto dishDto = dishServiceImpl.selById(id);
        return R.success(dishDto);
    }

    /**
     * 根据套餐id 查询dish列表
     *
     * @return
     */
    // 由于前端需要flavor中的信息，因此需要对该方法进行改造
/*    @GetMapping("/list")
    public R<List<Dish>> selListByCategory(Long categoryId){
        log.info("categoryId = {}",categoryId);
        List<Dish> dishes = dishServiceImpl.showListByCategory(categoryId);
        return R.success(dishes);
    }*/


    // 改进版本，增加了缓存
    @GetMapping("/list")
    public R<List<DishDto>> selListByCategory(Dish dish) {
        // 构造key
        String key = "dish_" + dish.getCategoryId() + "_" + dish.getStatus();
        List<DishDto> list = null;
        // 1、先去查看缓存中是否有数据
        list = (ArrayList<DishDto>) redisUtil.get(key);
        // 1.1 如果有，直接返回
        if (list != null) {
            return R.success(list);
        }
        // 1.2 如果没有，则查询数据库，将查询结果存入redis，再返回
        list = dishServiceImpl.showListByCategory(dish.getCategoryId());
        redisUtil.set(key, list, 60 * 60);
        return R.success(list);
    }

    /**
     * 修改菜品
     *
     * @return
     */
    @Transactional
    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto) {
        log.info("saving dish dishDto = {}", dishDto);
        System.out.println("描述:" + dishDto.getId());
        // redis清理缓存
        Long categoryId = dishDto.getCategoryId();
        Integer status = dishDto.getStatus();// 默认为1
        // 从缓存中清空categoryId所在分类下的菜品
        String key = "dish_" + categoryId + "_" + status;
        redisUtil.del(key);
        dishServiceImpl.update(dishDto);
        return R.success("修改菜品成功！");
    }

    /**
     * 删除菜品
     *
     * @param ids
     * @return
     */
    // 添加事务
    @Transactional
    @DeleteMapping
    public R<String> delete(Long[] ids) {
        log.info("ids = {}", ids);
        // ********因为起售商品无法删除，因此无需在删除操作的时候清空缓存********
       /* // 根据菜品id  查询他所在的分类，也就是查询categoryId
        for (Long id : ids) {
            Dish dish = dishServiceImpl.getById(id);
            // redis清理缓存
            Long categoryId = dish.getCategoryId();
            Integer status = dish.getStatus();// 默认为1
            // 从缓存中清空categoryId所在分类下的菜品
            if (status == 1){
                String key = "dish_" + categoryId + "_" + status;
                redisUtil.del(key);
            }
        }*/
        dishServiceImpl.deleteById(ids);
        return R.success("删除成功");
    }

    /**
     * 起售 ，批量起售
     * http://localhost/dish/status/0?ids=1548842038793936898
     */
    @PutMapping("/status/{status}")
    public R<String> updateById(@PathVariable Integer status,  Long[] ids){
        dishServiceImpl.updById(status,ids);
        return R.success("起售操作成功");
    }

}
