package org.ozm.trend.controller;

import org.ozm.trend.config.IpConfiguration;
import org.ozm.trend.pojo.Example;
import org.ozm.trend.pojo.Index;
import org.ozm.trend.pojo.User;
import org.ozm.trend.service.ExampleService;
import org.ozm.trend.service.IndexService;
import org.ozm.trend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.List;

//返回代码集合，并通过 IpConfiguration 获取当前接口并打印。
@RestController
public class IndexController {
    @Autowired
    IndexService indexService;
    @Autowired
    IpConfiguration ipConfiguration;
    @Autowired
    StringRedisTemplate stringRedisTemplate;
    @Autowired
    UserService userService;
    @Autowired
    ExampleService exampleService;

    //这里默认使用8081端口
    @GetMapping("/codes") //因为该方法在回测视图中涉及到访问访问另一个端口的服务，这属于跨域的范畴，所以这里要表明允许跨域
    @CrossOrigin
    public List<Index> codes() throws Exception{
        //打印当前端口
        System.out.println("current instance's port is "+ ipConfiguration.getPort());
        //在原有的公共实例的基础上添加用户导入的实例
        List<Index> result = indexService.get();
        //逻辑，将Example表中status为success的example数据取出，然后将对应的数据对象
        //name 和 code 赋值到Index对象上，最后放到List<Index>中
        String username = stringRedisTemplate.opsForValue().get("loginResponse");
        User user = userService.get(username);
        if (user!=null){
            List<Example> exampleList  = exampleService.get(user);
            for (Example example :exampleList){
                if ("success".equals(example.getStatus())){
                    Index index = new Index();
                    index.setCode(example.getCode());
                    index.setName(example.getName());
                    //在原有的基础上导入用户实例
                    result.add(index);
                }
            }
        }


        return result;
    }





}
