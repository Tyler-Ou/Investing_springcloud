package org.ozm.trend;


import brave.sampler.Sampler;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.NetUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Bean;

import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@SpringBootApplication
@EnableCaching
@EnableEurekaClient
//该微服务默认使用的端口为8011,又因为会做成集群，所以在集群里的微服务端口会定位8011,8012。。。
// 要启动多个端口需要在run/debug configuration上勾选allow paraller run
public class IndexCodesApplication {
    public static void main(String[] args) {
        int port = 0;
        int defaultPort = 8011;
        int redisPort = 6379;
        int eurekaServerPort = 8761;

        if(NetUtil.isUsableLocalPort(eurekaServerPort)) {
            System.err.printf("检查到端口%d 未启用，判断 eureka 服务器没有启动，本服务无法使用，故退出%n", eurekaServerPort );
            System.exit(1);
        }

        if(NetUtil.isUsableLocalPort(redisPort)) {
            System.err.printf("检查到端口%d 未启用，判断 redis 服务器没有启动，本服务无法使用，故退出%n", redisPort );
            System.exit(1);
        }

        //从参数中读出要切换为集群里其他微服务的端口
        if(null!=args && 0!=args.length) {
            for (String arg : args) {
                if (arg.startsWith("port=")){
                   String strPort = StrUtil.subAfter(arg,"port=",true);
                   if (NumberUtil.isNumber(strPort)){
                       port = Convert.toInt(strPort);
                   }
                }
            }
        }

        //到目前为止当端口暂为0时，参数上没有端口。管理可以通过在控制台上输入端口号去切换不同的微服务
        if(0==port) {
            Future<Integer> future = ThreadUtil.execAsync(() ->{
                int p = 0;
                System.out.printf("请于10秒钟内输入端口号, 推荐  %d ,超过10秒将默认使用 %d %n",defaultPort,defaultPort);
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
            //5秒内不输入端口则仍然为0，当然到了最后即为默认的端口8011
            try{
                //超时关闭线程
                port=future.get(10, TimeUnit.SECONDS);
            }//线程超时后，端口号为默认端口8011
            catch (InterruptedException | ExecutionException | TimeoutException e){
                port = defaultPort;
            }
        }

        if(!NetUtil.isUsableLocalPort(port)) {
            System.err.printf("端口%d被占用了，无法启动%n", port );
            System.exit(1);
        }
        new SpringApplicationBuilder(IndexCodesApplication.class).properties("server.port=" + port).run(args);

    }

    //链路追踪方法，用于追踪到该微服务 Sampler.ALWAYS_SAMPLE 表示一直在取样
    @Bean
    public Sampler defaultSampler() {
        return Sampler.ALWAYS_SAMPLE;
    }


}
