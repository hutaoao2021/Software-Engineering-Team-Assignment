package com.yrp.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yrp.dto.OrdersDto;
import com.yrp.pojo.Orders;
import org.springframework.transaction.annotation.Transactional;

/**
 * @Auther: 公众号/B站：是叶十三
 * @Date: 2022/7/21
 * @Description: com.yrp.service
 * @version: 1.0
 */
public interface OrdersService extends IService<Orders> {
    @Transactional
    void submit(Orders orders);

    Page<OrdersDto> page(Integer currientPage, Integer pageSize);

}
