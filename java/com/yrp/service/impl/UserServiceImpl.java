package com.yrp.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yrp.dao.UserDao;
import com.yrp.pojo.User;
import com.yrp.service.UserService;
import org.springframework.stereotype.Service;

/**
 * @Auther: 公众号/B站：是叶十三
 * @Date: 2022/7/19
 * @Description: com.yrp.service.impl
 * @version: 1.0
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserDao, User> implements UserService {
}
