package org.ozm.trend;


import cn.hutool.core.util.NetUtil;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

//注册中心主服务启动程序主入口
@SpringBootApplication
//表示它是注册中心服务器
@EnableEurekaServer
public class EurekaServerApplication {

    public static void main(String[] args) {
        //8761 这个端口是默认的，就不要修改了，后面的子项目，都会访问这个端口。
        int port = 8761;
        if(!NetUtil.isUsableLocalPort(port)) {
            System.err.printf("端口%d被占用了，无法启动%n", port );
            System.exit(1);
        }
        //多层次SpringContext时使用SpringApplicationBuilder来启动服务
        new SpringApplicationBuilder(EurekaServerApplication.class).properties("server.port=" + port).run(args);
    }
}
