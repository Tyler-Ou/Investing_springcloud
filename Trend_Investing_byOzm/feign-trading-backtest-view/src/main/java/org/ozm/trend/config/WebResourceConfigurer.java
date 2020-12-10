package org.ozm.trend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.util.ResourceUtils;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.FileNotFoundException;

//设置外部资源访问静态路径
@Configuration
public class WebResourceConfigurer implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        //addResourceHandler     是指你想在url请求的路径
        //addResourceLocations   是图片存放的真实路径
        //真实路径为当前工程的目录
        String fileFolder = null;
        try {
            fileFolder ="file:"+ ResourceUtils.getURL("classpath:").getPath()+"public/uploadFile/";
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        registry.addResourceHandler("/file/**").addResourceLocations(fileFolder);
    }

}
