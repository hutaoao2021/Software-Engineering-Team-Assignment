package com.yrp.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yrp.dao.EmployeeDao;
import com.yrp.pojo.Employee;
import com.yrp.service.EmployeeService;
import org.springframework.stereotype.Service;

/**
 * @Auther: 公众号/B站：是叶十三
 * @Date: 2022/7/14
 * @Description: com.yrp.service
 * @version: 1.0
 */
// 继承MyBatisPlus提供的父类ServiceImpl<EmployeeDao,Employee>，实现EmployeeService接口
@Service
public class EmployeeServiceImpl extends ServiceImpl<EmployeeDao, Employee> implements EmployeeService {

}
