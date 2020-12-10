package org.ozm.trend.service;

import cn.hutool.core.collection.CollUtil;
import org.ozm.trend.pojo.Index;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;


import java.util.List;

@Service
//服务类，直接从reids获取数据。 如果没有数据，则会返回 “无效指数代码”。
@CacheConfig(cacheNames = "indexes")
public class IndexService {
    List<Index> indexes; //虽然明面上没用到，但是由于CacheConfig的存在，会自动把指数的缓存存在该同名变量上

    //如果缓存中存在all_codes即从缓存中取出，如若不存在则调用方法，并将方法的结果返回到缓存上
    //这里直接读出会错误，因为在index-gather-store-service上已经存储在某个Index对象的缓存
    //虽然这里的index也同名，但是由于在index-gather-store-service存入的index和读出index（这里的index）
    //不一致，所以会导致json错误。所以在两个通用的Model里最好文件夹路径保持一致，在redis操作时，键值才会一致
    @Cacheable(key = "'all_codes'")
    public List<Index> get(){
        Index index = new Index();
        index.setName("无效指数代码");
        index.setCode("000000");
        return CollUtil.toList(index);
    }





}
