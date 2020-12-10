package org.ozm.trend.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.convert.Convert;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import org.ozm.trend.config.SpringContextUtil;
import org.ozm.trend.pojo.Address;
import org.ozm.trend.pojo.IndexData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
//同样地使用RestTemplate来获取数据，用nosql redis来存储数据
@Service
@CacheConfig(cacheNames = "index_datas")
public class IndexDataService {
    //其中String用来存储code，List<IndexData>用于存储数据
    private Map<String, List<IndexData>> indexDatas = new HashMap<>();
    @Autowired
    RestTemplate restTemplate;

    //断路器机制
    @HystrixCommand(fallbackMethod = "third_part_not_connected")
    public List<IndexData> fresh(String code){
        List<IndexData> indexDataList =fetch_indexes_from_third_part_index_data(code);
        indexDatas.put(code, indexDataList);

        System.out.println("code:"+code);
        System.out.println("indexeDatas:"+indexDatas.get(code).size());

        IndexDataService indexDataService = SpringContextUtil.getBean(IndexDataService.class);
        indexDataService.remove(code);
        return indexDataService.store(code);
    }

    //断路器方法
    public List<IndexData> third_part_not_connected(String code){
        System.out.println("third_part_not_connected()");
        IndexData index= new IndexData();
        index.setClosePoint(0);
        index.setDate("n/a");
        return CollectionUtil.toList(index);
    }


    //获取数据
    public List<IndexData> fetch_indexes_from_third_part_index_data(String code){
        String prefix = Address.LocalAddress_prefix;
        String address = Address.LocalAddress;
        String port = Address.LocalPort;
       // http://127.0.0.1:8090/indexes/000300.json
        List<Map> temp= restTemplate.getForObject(prefix+address+port+"/indexes/"+code+".json",List.class);
        return map2IndexData(temp);
        //将Map数据转换为indexData数据
    }

    public List<IndexData> map2IndexData(List<Map> temp){
        List<IndexData> indexDatas = new ArrayList<>();
        for(Map map:temp){
            String date = map.get("date").toString();
            float  closePoint = Convert.toFloat(map.get("closePoint"));
            IndexData indexData = new IndexData();
            indexData.setDate(date);
            indexData.setClosePoint(closePoint);
            indexDatas.add(indexData);
        }

        return indexDatas;
    }

    //从redis上获取缓存对象
    @Cacheable(key="'indexData-code-'+ #p0")
    public List<IndexData> get(String code){
        return CollUtil.toList();
    }


    //从redis上删除缓存对象
    @CacheEvict(key="'indexData-code-'+ #p0")
    public void remove(String code){}

//    在redis上保存缓存对象,indexServer类返回的大对象是List，这里返回的小对象List,而不是map
//    @CachePut：这个注释可以确保方法被执行，同时方法的返回值也被记录到缓存中。将变化后的值存储进缓存中
//    @Cacheable：当重复使用相同参数调用方法的时候，方法本身不会被调用执行，只有第一次被读取后，一直读缓存
//    即方法本身被略过了，取而代之的是方法的结果直接从缓存中找到并返回了。
    @CachePut(key = "'indexData-code-'+ #p0")
    public List<IndexData> store(String code){
        //返回对应code的List
        return indexDatas.get(code);
    }




}
