package com.yrp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yrp.common.BaseContext;
import com.yrp.common.Code;
import com.yrp.dao.OrdersDao;
import com.yrp.dto.OrdersDto;
import com.yrp.exception.BusinessException;
import com.yrp.pojo.*;
import com.yrp.service.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @Auther: 公众号/B站：是叶十三
 * @Date: 2022/7/21
 * @Description: com.yrp.service.impl
 * @version: 1.0
 */
@Service
public class OrdersServiceImpl extends ServiceImpl<OrdersDao, Orders> implements OrdersService {
    @Autowired
    private ShoppingCartService shoppingCartService;

    @Autowired
    private UserService userService;

    @Autowired
    private AddressBookService addressBookService;

    @Autowired
    private OrderDetailService orderDetailService;


    @Autowired
    private OrdersDao ordersDaoImpl;

    /**
     * 用户下单
     * @param orders
     */
    @Override
    public void submit(Orders orders) {
        //获得当前用户id
        Long userId = BaseContext.getCurrentId();
        //查询当前用户的购物车数据
        LambdaQueryWrapper<ShoppingCart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShoppingCart::getUserId,userId);
        List<ShoppingCart> shoppingCarts = shoppingCartService.list(wrapper);
        if(shoppingCarts == null || shoppingCarts.size() == 0){
            throw new BusinessException(Code.BUSINESS_ERR,"购物车为空，不能下单");
        }
        //查询用户数据
        User user = userService.getById(userId);
        //查询地址数据
        Long addressBookId = orders.getAddressBookId();
        AddressBook addressBook = addressBookService.getById(addressBookId);
        if(addressBook == null){
            throw new BusinessException(Code.BUSINESS_ERR,"用户地址为空，不能下单");
        }
        // 开始将数据存入订单表
        // 1、生成订单号
        long orderId = IdWorker.getId();//订单号
        AtomicInteger amount = new AtomicInteger(0);
        // 2、生成订单明细表  并且计算总金额amount
        List<OrderDetail> orderDetails = shoppingCarts.stream().map((item) -> {
            OrderDetail orderDetail = new OrderDetail(); // 创建订单明细实体
            orderDetail.setOrderId(orderId); // 设置订单号
            orderDetail.setNumber(item.getNumber()); // 设置菜品/套餐的数量
            orderDetail.setDishFlavor(item.getDishFlavor()); // 设置口味信息
            orderDetail.setDishId(item.getDishId()); // 设置菜品id号
            orderDetail.setSetmealId(item.getSetmealId());// 设置套餐id号
            orderDetail.setName(item.getName()); // 设置菜品/套餐名称
            orderDetail.setImage(item.getImage()); // 设置图片地址
            orderDetail.setAmount(item.getAmount()); // 设置菜品/套餐的单价
            amount.addAndGet(item.getAmount().multiply(new BigDecimal(item.getNumber())).intValue()); // 计算总金额
            return orderDetail;// 返回构造好之后的订单实体
        }).collect(Collectors.toList()); // 转成list集合

        orders.setId(orderId); //设置订单id
        orders.setOrderTime(LocalDateTime.now()); // 设置订单的下单时间
        orders.setCheckoutTime(LocalDateTime.now()); // 设置订单的结账时间（这里因为没有设计结账功能，因此结账时间也一并设置了）
        orders.setStatus(2); // 设置订单的状态（待派送）
        orders.setAmount(new BigDecimal(amount.get()));//总金额
        orders.setUserId(userId); // 设置下单用户的id
        orders.setNumber(String.valueOf(orderId)); // 设置订单号
        orders.setUserName(user.getName()); // 设置下单用户的姓名
        orders.setConsignee(addressBook.getConsignee()); // 设置收货人姓名
        orders.setPhone(addressBook.getPhone()); // 设置收货人手机
        // 设置收货地址
        orders.setAddress((addressBook.getProvinceName() == null ? "" : addressBook.getProvinceName())
                + (addressBook.getCityName() == null ? "" : addressBook.getCityName())
                + (addressBook.getDistrictName() == null ? "" : addressBook.getDistrictName())
                + (addressBook.getDetail() == null ? "" : addressBook.getDetail()));
        // 向订单表插入数据，一条数据
        ordersDaoImpl.insert(orders);
        // 向订单明细表中插入多条数据
        orderDetailService.saveBatch(orderDetails);
        //清空购物车数据
        shoppingCartService.remove(wrapper);
    }

    @Override
    public Page<OrdersDto> page(Integer currientPage, Integer pageSize) {
        // 构造分页器
        Page<Orders> page = new Page(currientPage, pageSize);
        // 用于存储结果
        Page<OrdersDto> pageDto = new Page<>();
        // 构造条件
        LambdaQueryWrapper<Orders> lqw = new LambdaQueryWrapper();
        ordersDaoImpl.selectPage(page, lqw);
        // 将page中的属性全部拷贝到pageDto，除了records
        BeanUtils.copyProperties(page, pageDto, "records");

        //  处理dishPageDto的records 构造出recordsDto
        List<Orders> records = page.getRecords();
        List<OrdersDto> recordsDto = new ArrayList<>();
        for (Orders item : records) {
            OrdersDto dto = new OrdersDto();
            // 属性拷贝
            BeanUtils.copyProperties(item, dto);

            // 根据订单id查询订单明细表
            Long orderId = item.getId();
            LambdaQueryWrapper<OrderDetail> lqw2 = new LambdaQueryWrapper();
            lqw2.eq(OrderDetail::getOrderId,orderId);
            List<OrderDetail> orderDetailsList = orderDetailService.list(lqw2);
            // 将订单明细设置到dto中
            dto.setOrderDetails(orderDetailsList);
            // 将dto添加到recordsDto集合中
            recordsDto.add(dto);
        }
        pageDto.setRecords(recordsDto);
        return pageDto;
    }
}
