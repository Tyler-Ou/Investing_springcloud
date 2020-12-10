package org.ozm.trend;

import brave.sampler.Sampler;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.NetUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * /**
 *  *
 *  *访问前面注册好的数据微服务。 springcloud 提供了两种方式，一种是 Ribbon，一种是 Feign
 *  * Ribbon 是使用 restTemplate 进行调用，并进行客户端负载均衡。
 *  *   Feign是Ribbon的封装，可以通过注解的方式实现HTTP请求以访问注册好的微服务
 *  * Ribbon和Feign都可以简单地理解为可以通过简洁的http请求从而实现负载均衡的客户端，
 *  * 通常用来为注册好的微服务实现负载均衡，负载均衡即由Ribbon和Feign根据性能的消耗去自我选择集群中的微服务
 *   现在用Feign client去访问数据微服务，在数据微服务上的存取数据上实现负载均衡
 * */

@SpringBootApplication
@EnableEurekaClient
@EnableDiscoveryClient
@EnableFeignClients //启动Feign client

public class FeignTradingBackTestServiceApplication {

    public static void main(String[] args) {
        int port = 0;
        int restTemplatePort = 8081;
        int defaultPort = 8051;
        int eurekaServerPort = 8761;

        if(NetUtil.isUsableLocalPort(eurekaServerPort)) {
            System.err.printf("检查到端口%d 未启用，判断 eureka 服务器没有启动，本服务无法使用，故退出%n", eurekaServerPort );
            System.exit(1);
        }

        if(null!=args && 0!=args.length) {
            for (String arg : args) {
                if(arg.startsWith("port=")) {
                    String strPort= StrUtil.subAfter(arg, "port=", true);
                    if(NumberUtil.isNumber(strPort)) {
                        port = Convert.toInt(strPort);
                    }
                }
            }
        }

        if(0==port) {
            Future<Integer> future = ThreadUtil.execAsync(()->{
                int p = 0;
                System.out.printf("请于5秒钟内输入端口号, 推荐  %d ,超过5秒将默认使用 %d ",defaultPort,defaultPort);
                Scanner scanner = new Scanner(System.in);
                while(true) {
                    String strPort = scanner.nextLine();
                    if(!NumberUtil.isInteger(strPort)) {
                        System.err.println("只能是数字");
                        continue;
                    }
                    else {
                        p = Convert.toInt(strPort);
                        scanner.close();
                        break;
                    }
                }
                return p;
            });
            try{
                port=future.get(5, TimeUnit.SECONDS);
            }
            catch (InterruptedException | ExecutionException | TimeoutException e){
                port = defaultPort;
            }
        }

        if(!NetUtil.isUsableLocalPort(port)) {
            System.err.printf("端口%d被占用了，无法启动%n", port );
            System.exit(1);
        }

        if(!NetUtil.isUsableLocalPort(restTemplatePort)) {
            System.err.printf("RestTemplate端口%d被占用了，无法启动%n", port );
            System.exit(1);
        }



        new SpringApplicationBuilder(FeignTradingBackTestServiceApplication.class).properties("server.port=" + port).run(args);

    }
    //RestTemplate 端口为8081
    @Bean
    RestTemplate restTemplate() {
        return new RestTemplate();
    }

    //链路追踪方法，用于追踪到该微服务 Sampler.ALWAYS_SAMPLE 表示一直在取样
    @Bean
    public Sampler defaultSampler() {
        return Sampler.ALWAYS_SAMPLE;
    }
}
