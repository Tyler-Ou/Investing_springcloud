package org.ozm.trend;

import brave.sampler.Sampler;
import cn.hutool.core.util.NetUtil;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableEurekaClient //在Eureka注册中心上可发现
//启动网关协议
@EnableZuulProxy
//设置在classPath中找寻对应的EurekaClient
@EnableDiscoveryClient
public class IndexZuulServiceApplication {
    //  http://127.0.0.1:8031/api-codes/codes
    public static void main(String[] args) {
        int port = 8031;
        if(!NetUtil.isUsableLocalPort(port)) {
            System.err.printf("端口%d被占用了，无法启动%n", port );
            System.exit(1);
        }
        new SpringApplicationBuilder(IndexZuulServiceApplication.class).properties("server.port=" + port).run(args);
    }

    //链路追踪方法，用于追踪到该微服务 Sampler.ALWAYS_SAMPLE 表示一直在取样
    @Bean
    public Sampler defaultSampler() {
        return Sampler.ALWAYS_SAMPLE;
    }


}
