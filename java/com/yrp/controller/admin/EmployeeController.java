package com.yrp.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yrp.common.Code;
import com.yrp.common.R;
import com.yrp.exception.BusinessException;
import com.yrp.exception.SystemException;
import com.yrp.pojo.Employee;
import com.yrp.service.EmployeeService;
import com.yrp.utils.MD5Utils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * @Auther: 公众号/B站：是叶十三
 * @Date: 2022/7/14
 * @Description: com.yrp.controller
 * @version: 1.0
 */
@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {
    @Autowired
    private EmployeeService employeeServiceImpl;

    /**
     * 员工登录
     *
     * @param request
     * @param employee
     * @return
     */
    @PostMapping("/login")
    public R<Employee> login(HttpServletRequest request, @RequestBody Employee employee) {
        //1、将页面提交的密码password进行md5加密处理
        String password = employee.getPassword();
        password = MD5Utils.code(password);

        //2、根据页面提交的用户名username查询数据库
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Employee::getUsername, employee.getUsername());
        Employee emp = employeeServiceImpl.getOne(queryWrapper);

        //3、如果没有查询到则返回登录失败结果
        if (emp == null) {
            return R.error("登录失败");
        }

        //4、密码比对，如果不一致则返回登录失败结果
        if (!emp.getPassword().equals(password)) {
            return R.error("登录失败");
        }

        //5、查看员工状态，如果为已禁用状态，则返回员工已禁用结果
        if (emp.getStatus() == 0) {
            return R.error("账号已禁用");
        }

        //6、登录成功，将员工id存入Session并返回登录成功结果
        request.getSession().setAttribute("employee", emp.getId());
        return R.success(emp);
    }

    /**
     * 员工退出
     *
     * @param request
     * @return
     */
    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request) {
        //清理Session中保存的当前登录员工的id
        request.getSession().removeAttribute("employee");
        return R.success("员工退出成功");
    }

    /**
     * 新增员工
     * @param request
     * @param employee
     * @return
     */
    @PostMapping
    public R<String> save(HttpServletRequest request, @RequestBody Employee employee) {
        log.info("新增员工，员工信息为：{}", employee.toString());
        //2、根据页面提交的用户名username查询数据库
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Employee::getUsername, employee.getUsername());
        Employee emp = employeeServiceImpl.getOne(queryWrapper);
        //3、如果在数据库中已存在
        if (emp != null) {
            throw new BusinessException(Code.BUSINESS_ERR, "该员工已存在！");
        }
        // 1、新增员工的时候，设置初始密码(000000),补充相关信息
        employee.setPassword(MD5Utils.code("000000"));
//        employee.setCreateTime(LocalDateTime.now());
//        employee.setUpdateTime(LocalDateTime.now());
//         2、获取当前登录用户，补充操作人
//        Long employeeId = (Long) request.getSession().getAttribute("employee");
//        employee.setUpdateUser(employeeId);
//        employee.setCreateUser(employeeId);
//        log.info("新增员工，员工信息为：{}", employee.toString());
        // 3、新增员工操作
        boolean flag = employeeServiceImpl.save(employee);
        if (flag == false) {
            throw new SystemException(Code.BUSINESS_ERR, "新增员工异常！");
        }
        System.out.println("flag:" + flag);
        return R.success("新增员工成功");
    }

    /**
     * @param currentPage 当前页码
     * @param pageSize 每页显示的跳数
     * @param name 按照name模糊查询
     * @return
     */
    /**
     * 员工信息分页查询
     *
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name) {
        log.info("page = {},pageSize = {},name = {}", page, pageSize, name);
        Page<Employee> p = new Page<>();
        p.setCurrent(page); //  当前的页码：3
        p.setSize(pageSize); //     每页显示的个条数：3
        //构造条件构造器
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper();
        //添加过滤条件
        queryWrapper.like(StringUtils.isNotEmpty(name), Employee::getName, name);
        //添加排序条件（按照更新时间降序排序）
        queryWrapper.orderByDesc(Employee::getUpdateTime);
        //执行查询
        employeeServiceImpl.page(p, queryWrapper);
        return R.success(p);
    }

    /**
     * 根据id修改员工信息
     * 禁用+启用+修改员工信息
     *
     * @param employee
     * @return
     */
    @PutMapping
    public R<String> update(HttpServletRequest request, @RequestBody Employee employee) {
        System.out.println(employee);
        log.info("修改员工信息========================");
//        Long employeeId = (Long) request.getSession().getAttribute("employee");
//        employee.setUpdateUser(employeeId);
//        employee.setUpdateTime(LocalDateTime.now());
        boolean b = employeeServiceImpl.updateById(employee);
        if (b == false) {
            throw new SystemException(Code.BUSINESS_ERR, "修改员工异常！");
        }
        return R.success("修改员工成功");
    }
    /**
     * 根据id查询员工信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<Employee> queryById(@PathVariable Long id) {
        Employee emp = employeeServiceImpl.getById(id);
        if (emp == null) {
            throw new SystemException(Code.BUSINESS_ERR, "查询员工异常！");
        }
        return R.success(emp);
    }
}