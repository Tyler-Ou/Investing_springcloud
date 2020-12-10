package org.ozm.trend.interceptor;

import org.ozm.trend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class ViewInterceptor implements HandlerInterceptor {
    //定义登录拦截器校验规则,发送请求前拦截
    @Autowired
    UserService userService;
    @Autowired
    StringRedisTemplate redisTemplate;
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        if (redisTemplate.opsForValue().get("loginResponse")==null){
            //未进行登录
            request.setAttribute("msg","没有权限请先登陆");
            request.getRequestDispatcher("/login").forward(request,response);
            return  false;
        }else{
            //已登录即直接放行
            return true;
        }

    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

    }
}
