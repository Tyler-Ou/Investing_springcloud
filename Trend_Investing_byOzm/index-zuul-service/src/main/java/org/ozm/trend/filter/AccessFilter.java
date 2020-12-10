package org.ozm.trend.filter;

import cn.hutool.core.util.StrUtil;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

//@Component
public class AccessFilter extends ZuulFilter {

    @Override
    public String filterType() {
        /**
         * pre：路由之前
         * routing：路由之时
         * post： 路由之后
         * error：发送错误调用
         */
        return FilterConstants.PRE_TYPE;
    }
    //过滤器执行顺序，当一个请求在同一个阶段存在多个过滤器的时候，多个过滤器执行顺序。
    @Override
    public int filterOrder() {
        return 0;
    }
    //判断过滤器是否生效
    @Override
    public boolean shouldFilter() {
        return true;
    }

    //编写过滤器拦截业务逻辑代码
    @Override
    public Object run() throws ZuulException{
        //1.获取上下文
        RequestContext currentContext = RequestContext.getCurrentContext();
        //2.获取Request对象  and Response
        HttpServletRequest request = currentContext.getRequest();
        HttpServletResponse response = currentContext.getResponse();
        //token对象
        String token = request.getHeader("token");

        //如果token字符串为空
        if(StrUtil.isEmpty((token))){
            token  = request.getParameter("token");
        }

        if (StrUtil.isEmpty((token))){
           // currentContext.setSendZuulResponse(false);
            currentContext.setResponseStatusCode(401);
        }

            return null;
    }


}
