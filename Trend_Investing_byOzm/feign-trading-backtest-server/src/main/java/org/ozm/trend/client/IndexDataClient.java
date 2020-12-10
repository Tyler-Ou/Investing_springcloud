package org.ozm.trend.client;

//以往数据微服务是通过restTemplate去访问数据模块的从而获取，
// 现在用Feign去访问数据微服务，在数据微服务上在存取数据上实现负载均衡
//Feign 工作原理：客户端，通过 注解方式 访问 访问INDEX-DATA-SERVICE服务的 /data/{code}路径，
// INDEX-DATA-SERVICE 既不是域名也不是ip地址，而是 数据服务在 eureka 注册中心的名称。

import org.ozm.trend.pojo.IndexData;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

//当INDEX-DATA-SERVICE微服务无法访问时，自动触发IndexDataClientFeignHystrix.class的断路
@FeignClient(value = "INDEX-DATA-SERVICE",fallback = IndexDataClientFeignHystrix.class)
public interface IndexDataClient {
    //通过指数代码获得对应数据
    @GetMapping("/data/{code}")  //与数据微服务的要访问的路径控制方法参数保存一致
    public List<IndexData> getIndexData(@PathVariable("code") String code);



}
