package com.yrp.controller.front;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yrp.common.R;
import com.yrp.dto.OrdersDto;
import com.yrp.pojo.Orders;
import com.yrp.service.OrdersService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @Auther: 公众号/B站：是叶十三
 * @Date: 2022/7/21
 * @Description: com.yrp.controller.front
 * @version: 1.0
 */
@RestController
@RequestMapping("/order")
@Slf4j
public class OrdersController {
    @Autowired
    private OrdersService ordersServiceImpl;

    // http://localhost/order/submit

    /**
     * 提交订单
     * @param orders
     * @return
     */
    @PostMapping("/submit")
    public R<String> submit(@RequestBody Orders orders){
        log.info("order = {}",orders);
        ordersServiceImpl.submit(orders);
        return R.success("下单成功");
    }

    private String userName;

    private String phone;

    private String address;

    private String consignee;
    @GetMapping("/userPage")
    public R<Page<OrdersDto>> showPage(Integer page, Integer pageSize){
        log.info("page = {}, pageSize = {}",page,pageSize);
        Page<OrdersDto> p =  ordersServiceImpl.page(page,pageSize);
        return R.success(p);
    }
}
