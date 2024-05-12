package com.yrp.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yrp.common.R;
import com.yrp.dto.SetmealDto;
import com.yrp.pojo.Setmeal;
import com.yrp.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Auther: 公众号/B站：是叶十三
 * @Date: 2022/7/18
 * @Description: com.yrp.controller
 * @version: 1.0
 */
//http://localhost/setmeal

@RestController
@RequestMapping("/setmeal")
@Slf4j
public class SetmealController {
    @Autowired
    private SetmealService setmealServiceImpl;
    @CacheEvict(value = "setmealCache", allEntries = true)
    @PostMapping
    public R<String> save(@RequestBody SetmealDto setmealDto) {
        log.info("setmealDto = {}",setmealDto);
        setmealServiceImpl.saveSetmeal(setmealDto);
        return R.success("新增套餐成功！");
    }

    /**
     * 分页查询，显示套餐信息
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    // 	http://localhost/setmeal/page?page=1&pageSize=10&name=1
    @GetMapping("/page")
    public R<Page<SetmealDto>> showPage(Integer page, Integer pageSize, String name){
        log.info("page = {}, pageSize = {}, name = {}",page,pageSize,name);
        Page<SetmealDto> pageInfo = setmealServiceImpl.getPageInfo(page,pageSize,name);
        return R.success(pageInfo);
    }


    // http://localhost/setmeal?ids=1415580119015145474
    @CacheEvict(value = "setmealCache", allEntries = true)
    @DeleteMapping
    public R<String> delete(@RequestParam List<Long> ids){
        log.info("ids = {}",ids);
        setmealServiceImpl.deleteByIds(ids);
        return R.success("删除套餐成功！");
    }


    //  http://localhost/setmeal/list?categoryId=1548149330848874498&status=1
    @Cacheable(value = "setmealCache", key = "'setmeal_'+ #setmeal.categoryId + '_' + #setmeal.status")
    @GetMapping("/list")
    public R<List<Setmeal>> list(Setmeal setmeal) {
        log.info("setmeal = {} ",setmeal);
        List<Setmeal> resList  = setmealServiceImpl.showList(setmeal);
        return R.success(resList);
    }
}
