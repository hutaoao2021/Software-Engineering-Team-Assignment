package com.yrp.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yrp.dto.SetmealDto;
import com.yrp.pojo.Setmeal;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @Auther: 公众号/B站：是叶十三
 * @Date: 2022/7/16
 * @Description: com.yrp.service
 * @version: 1.0
 */
public interface SetmealService extends IService<Setmeal> {

    /**
     * 新增套餐
     * 由于套餐的新增设计到另一张表setmeal_dish，因此需要给该方法添加事务
     * @param setmealDto
     */
    void saveSetmeal(SetmealDto setmealDto);

    /**
     * 分页查询
     * 由于这里需要显示套餐分类的名称，因此设计到另一个张表 分类表category
     * 可以通过categoryId 查询 出categoryName
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    Page<SetmealDto> getPageInfo(Integer page, Integer pageSize, String name);

    @Transactional
    void deleteByIds(List<Long> ids);

    List<Setmeal> showList(Setmeal setmeal);
}
