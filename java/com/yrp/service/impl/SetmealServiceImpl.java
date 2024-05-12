package com.yrp.service.impl;

import com.alibaba.druid.util.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yrp.common.Code;
import com.yrp.dao.SetmealDao;
import com.yrp.dto.SetmealDto;
import com.yrp.exception.BusinessException;
import com.yrp.pojo.Category;
import com.yrp.pojo.Setmeal;
import com.yrp.pojo.SetmealDish;
import com.yrp.service.CategoryService;
import com.yrp.service.SetmealDishService;
import com.yrp.service.SetmealService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Auther: 公众号/B站：是叶十三
 * @Date: 2022/7/16
 * @Description: com.yrp.service.impl
 * @version: 1.0
 */
@Service
public class SetmealServiceImpl extends ServiceImpl<SetmealDao, Setmeal> implements SetmealService {

    @Autowired
    private SetmealDishService setmealDishServiceImpl;

    @Autowired
    private SetmealDao setmealDaoImpl;

    @Autowired
    private CategoryService categoryServiceImpl;

    @Override
    public void saveSetmeal(SetmealDto setmealDto) {
        // 0、 先根据套餐名称去数据库表中搜索是否存在该套件
        //  0.1 如果存在该套餐，则向上层抛出异常，让全局异常捕获器进行捕获
        String name = setmealDto.getName();
        LambdaQueryWrapper<Setmeal> lqw = new LambdaQueryWrapper<>();
        lqw.eq(Setmeal::getName, name);
        Integer count = setmealDaoImpl.selectCount(lqw);
        if (count > 0) {
            throw new BusinessException(Code.BUSINESS_ERR, "该套餐已存在，请您修改后重试！");
        }
        //  0.2 如果不存在，则可以进行下面的的操作
        // 1、存入套餐信息
        int i = setmealDaoImpl.insert(setmealDto);
        if (i <= 0) {
            throw new BusinessException(Code.BUSINESS_ERR, "新增套餐异常，请您稍后重试！");
        }
        // 2、将菜品关系信息存入 套餐菜品关系表中
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        setmealDishes.stream().map((item) -> {
            item.setSetmealId(setmealDto.getId());
            return item;
        }).collect(Collectors.toList());
        boolean b = setmealDishServiceImpl.saveBatch(setmealDishes);
        if (b == false) {
            throw new BusinessException(Code.BUSINESS_ERR, "新增套餐异常，请您稍后重试！");
        }
    }

    @Override
    public Page<SetmealDto> getPageInfo(Integer page, Integer pageSize, String name) {
        Page<Setmeal> pageSetmeal = new Page<>(page, pageSize);
        Page<SetmealDto> pageInfo = new Page<>();
        // 构造查询条件
        LambdaQueryWrapper<Setmeal> lqw = new LambdaQueryWrapper();
        // 按照更新时间降序
        lqw.orderByAsc(Setmeal::getUpdateTime);
        // 若查询条件中存在套餐名称，则添加套餐名称这个查询条件
        lqw.like(!StringUtils.isEmpty(name), Setmeal::getName, name);
        setmealDaoImpl.selectPage(pageSetmeal, lqw);
        // 将pageSetmeal中所有的信息拷贝到当中pageInfo
        BeanUtils.copyProperties(pageSetmeal, pageInfo, "records");
        List<Setmeal> records = pageSetmeal.getRecords();
        List<SetmealDto> recordsDto = new ArrayList<>();
        for (Setmeal item : records) {
            // 1、创建一个setmealDto对象
            SetmealDto dto = new SetmealDto();
            // 3、拷贝属性
            BeanUtils.copyProperties(item, dto);
            // 3、根据categoryId查询获取到categoryName
            Long categoryId = item.getCategoryId();
            Category category = categoryServiceImpl.getById(categoryId);
            if (category == null) {
                throw new BusinessException(Code.BUSINESS_ERR, "显示套餐列表信息异常，请您稍后重试！");
            }
            String categoryName = category.getName();
            // 2、设置categoryName属性
            dto.setCategoryName(categoryName);
            recordsDto.add(dto);
        }
        // 4、给pageInfo填充数据
        pageInfo.setRecords(recordsDto);
        return pageInfo;
    }

    @Override
    public void deleteByIds(List<Long> ids) {
        /**
         1、判断套餐的状态
         1.1 如果存在起售状态，则无法删除，向高层抛出异常和提示信息
         1.2 如果不存在“停售”状态的套件，才能进行下面的操作
         2、根据ids批量删所有的套餐
         3、在setmeal_dish表中，根据setmeal_id批量删除带有该id的信息

         */
//        1、判断套餐的状态，对应的sql(select count(*) from dish where id in (..ids..) and status = 1)
        LambdaQueryWrapper<Setmeal> lqw = new LambdaQueryWrapper();
        lqw.eq(Setmeal::getStatus, 1);
        lqw.in(Setmeal::getId, ids);
        Integer index = setmealDaoImpl.selectCount(lqw);
        // 1.1 如果存在起售状态，则无法删除，向高层抛出异常和提示信息
        if (index > 0) {
            throw new BusinessException(Code.BUSINESS_ERR, "您要删除的套件中有正在售卖的套餐，无法删除！");
        }
        //1.2 如果不存在“停售”状态的套件，才能进行下面的操作
        // 2、根据ids批量删所有的套餐
        setmealDaoImpl.deleteBatchIds(ids);
        // 3、在setmeal_dish表中，根据setmeal_id批量删除带有该id的信息
        LambdaQueryWrapper<SetmealDish> lqw2 = new LambdaQueryWrapper();
        lqw2.in(SetmealDish::getSetmealId, ids);
        setmealDishServiceImpl.remove(lqw2);
    }

    @Override
    public List<Setmeal> showList(Setmeal setmeal) {
        // 根据categoryId 和 status = 1 查询所有套餐信息
        LambdaQueryWrapper<Setmeal> lqw = new LambdaQueryWrapper();
        // 设置查询条件
        lqw.eq(setmeal.getCategoryId() != null , Setmeal::getCategoryId,setmeal.getCategoryId());
        lqw.eq(setmeal.getStatus() != null,Setmeal::getStatus,setmeal.getStatus());
        // 查询结果排序方式
        lqw.orderByAsc(Setmeal::getUpdateTime);
        List<Setmeal> setmealList = setmealDaoImpl.selectList(lqw);
        return setmealList;
    }
}
