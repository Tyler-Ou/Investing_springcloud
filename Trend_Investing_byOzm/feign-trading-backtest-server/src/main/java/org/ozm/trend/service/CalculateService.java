package org.ozm.trend.service;

import org.ozm.trend.pojo.IndexData;
import org.ozm.trend.pojo.Profit;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CalculateService {
    //按照一定的ma值，购买阈值，出售阈值对某个indexData计算预测利润
    public Map<String,Object> result(IndexData indexData,float cash,int ma, float buyThreshold, float sellThreshold, float closePoint){
        List<Profit> profits =new ArrayList<>();
        int winCount = 0;//盈利次数
        float totalWinRate = 0;//
        float avgWinRate = 0;//平均盈利
        float totalLossRate = 0;
        int lossCount = 0;//亏损次数
        float avgLossRate = 0;//平均亏损

        float init = cash;
        float share = 0; //存量


        return new HashMap<>();

    }

}
