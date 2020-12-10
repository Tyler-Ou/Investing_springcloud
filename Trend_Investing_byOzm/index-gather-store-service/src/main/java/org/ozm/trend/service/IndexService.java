package org.ozm.trend.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import org.ozm.trend.config.SpringContextUtil;
import org.ozm.trend.pojo.Address;
import org.ozm.trend.pojo.Index;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

//该类为数据采集类，数据采集使用RestTemplate的方式到第三方模块中去采集
//RestTemplate是Spring提供的用于访问Rest服务的客户端，RestTemplate提供了多种便捷访问远程Http服务的方法
@Service
@CacheConfig(cacheNames="indexes") //启用缓存供redis使用
public class IndexService {
    @Autowired
    RestTemplate restTemplate;
    //采集到的数据以index的数据类型进行保存，index的属性为code和name
    private List<Index> indexes;

    //从“第三方”模块中 采集数据，
    //这里采取断路器，如果当第三方 服务的模块没有启动时 不再请求此模块，以致资源被占用
    //当断路器触发后调用 third_part_not_connected以刷新数据
    //新模块，数据可刷新。一般逻辑为：
    //1、先运行 fetch_indexes_from_third_part 来获取数据
    //2、删除数据
    //3、保存数据
    @HystrixCommand(fallbackMethod = "third_part_not_connected")
    public List<Index> fresh() {
        //这里用了 SpringContextUtil.getBean 去重新获取了一次 IndexService，为什么不直接在 fresh 方法里调用 remove, store 方法， 而要重新获取一次呢？
        //这个是因为 springboot 的机制大概有这么个 bug吧.
        // 从已经存在的方法里调用 redis 相关方法，并不能触发 redis 相关操作，
        // 所以只好用这种方式重新获取一次了。
        indexes =fetch_indexes_from_third_part_index_data();
        //重新获取一次indexService对象，然后进行删除
        IndexService indexService = SpringContextUtil.getBean(IndexService.class);
        indexService.remove();
        return indexService.store();
    }




//    原模块，没有刷新功能直接获取数据的方法
//    @Cacheable(key="'all_codes'")
//    public List<Index> fetch_indexes_from_third_part_index_data(){
//        //返回的数据是键值对数据，即Map类型,但要存储在index上，所以要转换为index数据类型
//        List<Map> mapList=restTemplate.getForObject("http://127.0.0.1:8090/indexes/codes.json",List.class);
//        return  map2Index(mapList);
//    }

    //断路器
    public List<Index> third_part_not_connected(){
        System.out.println("third_part_not_connected()");
        Index index= new Index();
        index.setCode("000000");
        index.setName("无效指数代码");
        return CollectionUtil.toList(index);
    }

    public List<Index> fetch_indexes_from_third_part_index_data(){
        //返回的数据是键值对数据，即Map类型,但要存储在index上，所以要转换为index数据类型
        String prefix = Address.LocalAddress_prefix;
        String address = Address.LocalAddress;
        String port = Address.LocalPort;
        List<Map> mapList=restTemplate.getForObject(prefix+address+port+"/indexes/codes.json",List.class);
        return  map2Index(mapList);
    }



    //将map类型转换为index类型
    private List<Index> map2Index(List<Map> mapList){
        List<Index> indexList = new ArrayList<>();
        for (Map map:mapList) {
            //读出map数据
            String code = map.get("code").toString();
            String name = map.get("name").toString();
            Index index = new Index();
            index.setCode(code);
            index.setName(name);
            indexList.add(index);
        }
        return indexList;
    }


    //从Redis中获取数据 如果有缓存数据则直接返回注解结果，如果没有则返回方法对象到注解上
    @Cacheable(key="'all_codes'")
    public List<Index> get(){
        //如果第一次获取数据也没有缓存的时候则返回空数据
        return CollUtil.toList();
    }

    //清空redis上的数据
    @CacheEvict(allEntries=true)
    public void remove(){
    }

    //保存数据到redis上，如果有缓存数据则直接返回注解结果，如果没有则返回方法对象到注解上
    @CachePut(key = "'all_codes'")
    public List<Index> store(){
        return indexes;
    }




}
