package org.ozm.trend.server;

import cn.hutool.core.collection.CollUtil;
import org.ozm.trend.pojo.IndexData;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@CacheConfig(cacheNames = "index_datas")
public class IndexDataService {

    private List<IndexData> index_datas;

    //如果存在改缓存则取出,不存在缓存则返回空
    @Cacheable(key = "'indexData-code-'+#p0")
    public List<IndexData> get(String code){
        return CollUtil.toList();
    }



}
