package org.ozm.trend.controller;

import cn.hutool.core.util.StrUtil;
import cn.hutool.db.Session;
import org.ozm.trend.entity.User;
import org.ozm.trend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Map;

@Controller
public class LoginController {

    @Autowired
    UserService userService;
    @Autowired
    StringRedisTemplate redisTemplate;
    @PostMapping("/login")
    public String login(
            @RequestParam("username")String username,
            @RequestParam("password")String password,
            Map<String,Object> map, HttpServletResponse response
    ){
        if (!StrUtil.isEmpty(username)&&!StrUtil.isEmpty(password)) {
            User user = userService.login(username, password);
            //1、从数据库中校验是否存在对应用户和其对应的密码
            //2、如果user不为空即将对应user存储在redis上，设计一个存储对应redis user 的session
            // 因为数据涉及跨域。所以要共享session须使用redis
            //3、每个页面在访问前都需要校验一次session即用户是否登录 设计一个校验redis的user session 是否存在的方法

            if (null == user) {
                map.put("msg", "账号或密码有误");
                // System.out.println("123");
                return "login";
            } else {
                //将账户登录信息存储在redis上
                userService.redis_save(username);
                redisTemplate.opsForValue().set("loginResponse",user.getUsername());
                return "view";
            }
        }else{
            //System.out.println("123");
            map.put("msg", "账号或密码有误");
            return "login";
        }

    }
}
