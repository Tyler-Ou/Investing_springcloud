package org.ozm.trend.job;


//定时器用于每日定时更新数据，使用定时器工具 Quartz 来实现即可

import cn.hutool.core.date.DateUtil;
import org.ozm.trend.pojo.Index;
import org.ozm.trend.service.IndexDataService;
import org.ozm.trend.service.IndexService;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.util.List;

public class IndexDataSyncJob extends QuartzJobBean {

    //声明要自启的对象
    @Autowired
    IndexService indexService;
    @Autowired
    IndexDataService indexDataService;


    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        System.out.println("定时启动：" + DateUtil.now());
        List<Index> indices = indexService.fresh();
        //读出index对象，并刷新对应index对象的数据
        for(Index index:indices){
            indexDataService.fresh(index.getCode());
        }
        System.out.println("定时结束：" + DateUtil.now());


    }
}
