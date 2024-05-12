package com.yrp.controller.front;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yrp.common.Code;
import com.yrp.common.R;
import com.yrp.common.SMSUtils;
import com.yrp.exception.BusinessException;
import com.yrp.pojo.User;
import com.yrp.service.UserService;
import com.yrp.utils.RedisUtil;
import com.yrp.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @Auther: 公众号/B站：是叶十三
 * @Date: 2022/7/19
 * @Description: com.yrp.controller.front
 * @version: 1.0
 */
// http://localhost/user/sendMsg
@RestController
@Slf4j
@RequestMapping("/user")
public class UserController extends SMSUtils {
    @Autowired
    private UserService userServiceImpl;

    /*引入工具类*/
    @Autowired
    private RedisUtil redisUtil;

    // http://localhost/user/19979243462
    @GetMapping("/{id}")
    public R<User> getUserInfoById(@PathVariable Long id){
        log.info("id=  {}",id);
        User user = userServiceImpl.getById(id);
        if (user == null ){
            throw new BusinessException(Code.BUSINESS_ERR,"用户信息查询失败！");
        }
        return R.success(user);
    }
    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user, HttpServletRequest req){
        // 1、获取手机号
            String phone = user.getPhone();
        if (StringUtils.isEmpty(phone)){
            R.error("验证码发送失败！");
        }
        // 生成4位的验证码
        String code = String.valueOf(ValidateCodeUtils.generateValidateCode(4)); //
        log.info("当前发送的验证码为{}",code);
        // 发送验证码
//            sendSMS(phone,code,"");
        // 将生成的验证码存入session，后期
//        req.getSession().setAttribute("phone",code);
        //改进版本：将其存入redis，设置有效时间为5分钟
        redisUtil.set("phone",code,300);
        return R.success("验证码发送成功！");
    }
    // http://localhost/user/login
    @PostMapping("/login")
    public R<User> login(@RequestBody Map map, HttpServletRequest req){
        log.info("map = {}",map);
        // 获取手机号
        String phone = (String)map.get("phone");
        // 获取验证码
        String code = (String)map.get("code");
        // 将session中保存的验证码拿出来与当前验证码比对(废弃该方式)
//        String trueCode = (String)req.getSession().getAttribute("phone");
        // 从redis中拿出验证码
        String trueCode = (String)redisUtil.get("phone");
        //  如果比对失败，登录失败
        if (trueCode != null && !trueCode.equals(code)){
            return R.error("验证码错误！");
        }
        //  如果比对成功，则说明登录成功
            //判断该用户是否已经注册过
        LambdaQueryWrapper<User> lqw = new LambdaQueryWrapper<>();
        lqw.eq(User::getPhone,phone);
        User u = userServiceImpl.getOne(lqw);
        // 如果没有注册，帮其注册（自动注册功能）
        if (u == null){
            u = new User();
            u.setId(Long.parseLong(phone));
            u.setPhone(phone);
            u.setStatus(1);
            // 生成随机的用户名
            Integer newName = ValidateCodeUtils.generateValidateCode(6);
            u.setName("yrpUser_"+newName);
            userServiceImpl.save(u);
        }
        // 返回之前：先将用户存入session
        req.getSession().setAttribute("user",phone);
        // 如果已经注册，则直接跳转到首页
        // 如果用户已经登录成功，需要将旧验证码从缓存中清除掉
        redisUtil.del("phone");
        return R.success(u);
    }

    // http://localhost/user/loginout

    /**
     * 退出登录
     * @param request
     * @return
     */
    @PostMapping("/loginout")
    public R<String> logout(HttpServletRequest request) {
        //清理Session中保存的当前登录员工的id
        request.getSession().removeAttribute("employee");
        return R.success("用户退出成功");
    }

}
