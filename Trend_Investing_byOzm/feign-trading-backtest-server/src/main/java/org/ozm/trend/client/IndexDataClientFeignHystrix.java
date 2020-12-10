package org.ozm.trend.client;

import cn.hutool.core.collection.CollectionUtil;
import org.ozm.trend.pojo.IndexData;
import org.springframework.stereotype.Component;

import java.util.List;

//当微服务未生效并发生熔断时该方法触发
@Component
public class IndexDataClientFeignHystrix implements IndexDataClient {
    @Override
    public List<IndexData> getIndexData(String code) {
        IndexData indexData = new IndexData();
        indexData.setClosePoint(0);
        indexData.setDate("0000-00-00");
        return CollectionUtil.toList(indexData);
    }

}
