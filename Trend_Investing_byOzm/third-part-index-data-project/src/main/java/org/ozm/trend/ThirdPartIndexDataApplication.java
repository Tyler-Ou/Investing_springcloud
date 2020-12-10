package org.ozm.trend;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.NetUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@SpringBootApplication
@EnableEurekaClient //代表该模块注册为微服务
public class ThirdPartIndexDataApplication {

    public static void main(String[] args) {

        int port = 8090; //定义启动该微服务的端口
        int eurekaServerPort = 8761; //定义注册中心的端口
        //检测注册中心是否启动
        if (NetUtil.isUsableLocalPort(eurekaServerPort)){
            System.err.printf("检查到端口%d 未启用，判断 eureka 服务器没有启动，本服务无法使用，故退出%n", eurekaServerPort );
            System.exit(1); //退出
        }

        //通过校验提交到args上的port参数，并将port取出 灵活地定义该微服务上的集群的不同端口
        //参数的定义可以通过在edit Configuration 的program argument 上定义，定义多个以空格分隔
        if (null!=args && 0!=args.length){
            //因为args是字符串数组，所以要每个单独取出来
            for (String arg : args){
                //判断agr是否以port=作为开头
                if(arg.startsWith("port=")) {
                    //java截取分隔字符串之后字符串不包括分隔字符串,例如 port = 9999 即截取9999
                    // int pos = isLastSeparator ? str.lastIndexOf(sep) : str.indexOf(sep);
                    String strPort= StrUtil.subAfter(arg, "port=", true);
                    if(NumberUtil.isNumber(strPort)) {
                        port = Convert.toInt(strPort); //将字符串强行转换为整型,与Parse不同的是，返回0不会跳出异常
                    }
                }
            }
        }

        //检测该微服务的端口是否被占用，若没被占用，则以该端口开展微服务
        if (!NetUtil.isUsableLocalPort(port)){
            System.err.printf("端口%d被占用了，无法启动%n", port );
            System.exit(1); //退出
        }

        //端口未被占用时，以该端口启动该服务
        new SpringApplicationBuilder(ThirdPartIndexDataApplication.class).properties("server.port=" + port).run(args);




    }


}
