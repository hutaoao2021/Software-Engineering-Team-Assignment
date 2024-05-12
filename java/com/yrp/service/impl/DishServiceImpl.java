package com.yrp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yrp.common.Code;
import com.yrp.dao.DishDao;
import com.yrp.dto.DishDto;
import com.yrp.exception.BusinessException;
import com.yrp.pojo.Category;
import com.yrp.pojo.Dish;
import com.yrp.pojo.DishFlavor;
import com.yrp.service.CategoryService;
import com.yrp.service.DishFlavorService;
import com.yrp.service.DishService;
import com.yrp.utils.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @Auther: 公众号/B站：是叶十三
 * @Date: 2022/7/16
 * @Description: com.yrp.service.impl
 * @version: 1.0
 */
@Slf4j
@Service
public class DishServiceImpl extends ServiceImpl<DishDao, Dish> implements DishService {
    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private DishDao dishDaoImpl;

    @Autowired
    private DishFlavorService dishFlavorServiceImpl;

    @Autowired
    private CategoryService categoryServiceImpl;

    // 添加事务
    @Transactional
    @Override
    public void save(DishDto dishDto) {
        // 1、存入dish表
        int i = dishDaoImpl.insert(dishDto);
        if (i < 0) {
            throw new BusinessException(Code.BUSINESS_ERR, "添加菜品失败，请稍后重试！");
        }
        log.info("dishId = {}", dishDto.getId());
        // 注意：这里dish存入dish表后，会通过雪花算法自动生成一个id，这时候，我们就能够get到id
        Long dishId = dishDto.getId();
        // 2、dishflavor (还差了一个dishId字段)
        List<DishFlavor> flavors = dishDto.getFlavors();
        //   2.1 如果dishflavor为空，则不用存
        if (flavors.size() <= 0){
            return;
        }

        //   2.2 如果dishflavor为空，则需要先存入
        for (DishFlavor flavor : flavors) {
            flavor.setDishId(dishId);
        }
        boolean b = dishFlavorServiceImpl.saveBatch(flavors);
        if (b == false) {
            throw new BusinessException(Code.BUSINESS_ERR, "添加菜品失败，请稍后重试！");
        }
    }
    /**
     * 分页查询（设计到两张表，除了当前表外，还需要需要另一张表category中（name））
     * 可以通过category_id 查询到分类表中的名称
     *
     * @param currentPage
     * @param pageSize
     * @param name
     * @return
     */
    @Override
    public Page<DishDto> selPage(int currentPage, int pageSize, String name) {
        Page<Dish> dishPage = new Page<>(currentPage, pageSize);

        // 返回结果
        Page<DishDto> dishPageDto = new Page<>();


        // 过滤条件
        LambdaQueryWrapper<Dish> lqw = new LambdaQueryWrapper<>();
        // sql拼接
        if (name != null) {
            lqw.like(Dish::getName, name);
        }
        // 安装updateTime降序
        lqw.orderByDesc(Dish::getUpdateTime);
        // 分页查询
        dishDaoImpl.selectPage(dishPage, lqw);

        // 将dishPage 拷贝到 dishPageDto，除了records属性
        BeanUtils.copyProperties(dishPage, dishPageDto, "records");
        // 处理dishPageDto的records
        List<Dish> records = dishPage.getRecords();

        List<DishDto> recordsDto = new ArrayList<>();
        for (Dish record : records) {
            DishDto dto = new DishDto();
            // 1、将dish中其他属性拷贝到dishDto中
            BeanUtils.copyProperties(record, dto);
            Long categoryId = record.getCategoryId(); // 分类id
            log.info("categoryId = {}",categoryId);
            // 2、通过categoryId 去category表中查询出categoryName
            Category category = categoryServiceImpl.getById(categoryId);
            String categoryName = category.getName();
            // 3、给dto设置分类名称
            dto.setCategoryName(categoryName);
            // 4、将dto设置给records
            recordsDto.add(dto);
        }
        dishPageDto.setRecords(recordsDto);
        return dishPageDto;
    }

    @Override
    public DishDto selById(Long id) {
        // 构建返回值
        DishDto dto = new DishDto();
        Dish dish = dishDaoImpl.selectById(id);
        if (dish == null) {
            throw new BusinessException(Code.BUSINESS_ERR, "你操作的菜品丢失，请稍后重试");
        }
        LambdaQueryWrapper<DishFlavor> lmq = new LambdaQueryWrapper<>();
        lmq.eq(DishFlavor::getDishId, id);
        List<DishFlavor> list = dishFlavorServiceImpl.list(lmq);
        if (list == null) {
            throw new BusinessException(Code.BUSINESS_ERR, "你操作的菜品丢失，请稍后重试");
        }
        BeanUtils.copyProperties(dish, dto);
        dto.setFlavors(list);
        return dto;
    }

    /**
     * 设计到两张表：口味表dishFlavor + 菜品表dish
     *
     * @param dishDto
     */
    @Override
    public void update(DishDto dishDto) {
        /**
         1、根据id查询dish表，看能否该菜品是还存在
         1.1 如果不存在，往上层抛出异常，让全局异常捕获器去捕获
         1.2 如果存在，将当前菜品修改（通过id字段相同）
         2、所谓的修改口味表：实质上可行的方式是-先通过dish_id，将与该dish_id关联的口味信息全部删除，然后重新插入
         2.1 将与该dish_id关联的口味信息全部删除
         2.2 重新插入口味
         */

        // 1、根据id查询dish表，看能否该菜品是还存在
        //  1.1 如果不存在，往上层抛出异常，让全局异常捕获器去捕获
        Long dishId = dishDto.getId();
        Dish dish = dishDaoImpl.selectById(dishId);
        if (dish == null) {
            throw new BusinessException(Code.BUSINESS_ERR, "修改异常，该菜品已不存在！");
        }
        // 1.2 如果存在，将当前菜品修改（通过id字段相同）
        // 1.2.1 如果修改失败，往上层抛出异常
        int index = dishDaoImpl.updateById(dishDto);
        if (index <= 0) {
            throw new BusinessException(Code.BUSINESS_ERR, "修改异常，请稍后重试！");
        }
        // 1.2.2 如果修改成功，继续下面操作
        // 2、所谓的修改口味表：实质上可行的方式是-先通过dish_id，将与该dish_id关联的口味信息全部删除，然后重新插入
        // 2.1 将与该dish_id关联的口味信息全部删除
        LambdaQueryWrapper<DishFlavor> lqm = new LambdaQueryWrapper();
        lqm.eq(DishFlavor::getDishId, dishId);
        dishFlavorServiceImpl.remove(lqm);
        // 2.2 重新插入口味
        List<DishFlavor> flavors = dishDto.getFlavors();
        // 在插入之前给其设置dishId
        for (DishFlavor df : flavors) {
            df.setDishId(dishId);
        }
        dishFlavorServiceImpl.saveBatch(flavors);
    }

    @Override
    public void deleteById(Long[] ids) {
        List<Long> list = Arrays.asList(ids);
        // 根据dish_id批量删除flower
        LambdaQueryWrapper<DishFlavor> lqw = new LambdaQueryWrapper();
        for (Long id : ids) {
            // 根据id 查询status
            // 如果存在status =1 ，即起售状态，则该菜品不能被删除，抛出异常，给出提示信息给前端
            Dish dish = dishDaoImpl.selectById(id);
            if (dish.getStatus() == 1){
                throw new BusinessException(Code.BUSINESS_ERR,"菜品删除失败，起售商品不能被删除！");
            }
            lqw.eq(DishFlavor::getDishId, id);
        }
        dishFlavorServiceImpl.remove(lqw);
        // 根据id批量删除dish
        int i = dishDaoImpl.deleteBatchIds(list);
        if (i <= 0) {
            throw new BusinessException(Code.BUSINESS_ERR, "菜品不存在，删除菜品失败！");
        }
    }

    @Override
    public void updById(Integer status, Long[] ids) {
        // 1、封装一个dish对象
        Dish dish = new Dish();
        dish.setStatus(status);
        // 2、开始更新操作
        for (Long id : ids) {
            dish.setId(id);
            int i = dishDaoImpl.updateById(dish);

        }
      // 根据菜品id  查询他所在的分类，也就是查询categoryId
        for (Long id : ids) {
            Dish d = dishDaoImpl.selectById(id);
            // redis清理缓存
            Long categoryId = d.getCategoryId();
            // 从缓存中清空categoryId所在分类下的菜品
            String key = "dish_" + categoryId + "_1";
            redisUtil.del(key);
        }
    }
    @Override
    public List<DishDto> showListByCategory(Long categoryId) {
        List<DishDto> resList = new ArrayList<>();
        // 1、获取到dish表中属性categoryId分类下的所有信息
        LambdaQueryWrapper<Dish> lqw = new LambdaQueryWrapper<>();
        lqw.eq(Dish::getCategoryId, categoryId);
        // 起售状态：1
        lqw.eq(Dish::getStatus,1);
        List<Dish> dishList = dishDaoImpl.selectList(lqw);
        // 2、获取1中得到的dishList所对应的口味信息
        for (Dish dish : dishList) {
            DishDto dto = new DishDto();
            Long id = dish.getId();
            // 根据dish_id获取到对应的口味信息
            LambdaQueryWrapper<DishFlavor> lqw2 = new LambdaQueryWrapper();
            lqw2.eq(DishFlavor::getDishId,id);
            List<DishFlavor> list = dishFlavorServiceImpl.list(lqw2);
            // 拷贝属性
            BeanUtils.copyProperties(dish,dto);
            dto.setFlavors(list);
            resList.add(dto);
        }
        return resList;
    }
}
