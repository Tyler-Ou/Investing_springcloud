package org.ozm.trend;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.NetUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.netflix.discovery.util.StringUtil;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

//采集使用的是 RestTemplate 方式来做， 存储本地我们会采用 nosql redis 来保存。
//注意的是RestTemplate是一个Rest服务器，所以要另外将它作为bean添加进容器上
@SpringBootApplication
@EnableEurekaClient //表明该模块是eureka微服务
@EnableHystrix  //启动断路器，避免集群或者分布式服务中某个连接端口失败后产生一系列的连锁反而最终导致服务瘫痪。当某个端口失败时，断路器自动断路，切换至其他端口的线程
@EnableCaching //启用缓存供redis使用
public class IndexGatherStoreApplication {
    public static void main(String[] args) {
            int port = 0; //该port可以通过参数进行定义
            int defaultPort  = 8001;
            int eurekaServerPort = 8761;
            int redisPort = 6379;
            port = defaultPort;
            //校验注册中心端口是否被占用
            if(NetUtil.isUsableLocalPort(eurekaServerPort)) {
                System.err.printf("检查到端口%d 未启用，判断 eureka 服务器没有启动，本服务无法使用，故退出%n", eurekaServerPort );
                System.exit(1);
            }

            //校验redis端口是否启用
            if (NetUtil.isUsableLocalPort(redisPort)){
                System.err.printf("检查到端口%d 未启用，判断 redis 服务器没有启动，本服务无法使用，故退出%n", eurekaServerPort );
                System.exit(1);
            }




            if (null !=args && 0!=args.length){
                for (String arg:args){
                    if (arg.startsWith("port=")){
                        //从参数中截取Port=xxxx后的xxxx作为端口
                        String strPort = StrUtil.subAfter(arg,"port=",true);
                        if(NumberUtil.isNumber(strPort)) {
                            port = Convert.toInt(strPort);
                        }
                    }
                }
            }
            //校验当前模块指定端口是否被占用，如果没被占用，则使用该端口作为该微服务的端口
            if(!NetUtil.isUsableLocalPort(port)) {
                System.err.printf("端口%d被占用了，无法启动%n", port );
                System.exit(1);
            }
            //启动该微服务
            new SpringApplicationBuilder(IndexGatherStoreApplication.class).properties("server.port=" + port).run(args);

    }

    @Bean
    RestTemplate restTemplate() {
        return new RestTemplate();
    }

}
