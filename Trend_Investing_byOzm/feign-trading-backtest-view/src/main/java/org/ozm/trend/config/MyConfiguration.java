package org.ozm.trend.config;

import org.ozm.trend.interceptor.ViewInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
public class MyConfiguration  extends WebMvcConfigurerAdapter{

    @Bean
    public ViewInterceptor getLoginIntercepter() {
        return new ViewInterceptor();
    }
    //注册登录拦截器
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(getLoginIntercepter()).addPathPatterns("/","/view","/result","/userconsole").
                excludePathPatterns("/login");
    }
}
