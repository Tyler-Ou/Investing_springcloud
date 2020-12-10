package org.ozm.trend.controller;

import cn.hutool.core.util.StrUtil;
import org.ozm.trend.entity.User;
import org.ozm.trend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Map;


@Controller
public class ViewController {

    @Autowired
    UserService userService;

    @Autowired
    StringRedisTemplate redisTemplate;

    @GetMapping("/")
    public String view() throws Exception {
        return "view";
    }

    @GetMapping("/view")
    public String viewTo() throws Exception {
        return "view";
    }


    @GetMapping("/login")
    public String login() throws Exception{
        //校验redis上是否存在已登录的账号，依据此便可进行登录拦截的条件
        //System.out.println(redisTemplate.opsForValue().get("loginResponse"));
        //boolean isExist = userService.isExist();
        if (redisTemplate.opsForValue().get("loginResponse")!=null){
            return "view";
        }
        return "login";
    }

    //注销
    @GetMapping("/signout")
    public String signout()throws Exception{
        //清除redis上的登录信息
       if (redisTemplate.opsForValue().get("loginResponse")!=null) {
           redisTemplate.delete("loginResponse");
       }
        return "login";
    }



    @GetMapping("/result")
    public String result()throws Exception{

        return "resultView";
    }

    //工作台 post请求为提交axaj实例
    @GetMapping("/userconsole")
    public String console()throws Exception{
        return "userConsoleView";
    }


    @GetMapping("/calculate")
    public String calculate()throws Exception{
        return "AnalyseCalculate";
    }

}
