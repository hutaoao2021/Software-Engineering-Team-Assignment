package com.yrp.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yrp.dao.OrderDetailDao;
import com.yrp.pojo.OrderDetail;
import com.yrp.service.OrderDetailService;
import org.springframework.stereotype.Service;

/**
 * @Auther: 公众号/B站：是叶十三
 * @Date: 2022/7/21
 * @Description: com.yrp.service.impl
 * @version: 1.0
 */
@Service
public class OrderDetailServiceImpl extends ServiceImpl<OrderDetailDao, OrderDetail> implements OrderDetailService {
}
