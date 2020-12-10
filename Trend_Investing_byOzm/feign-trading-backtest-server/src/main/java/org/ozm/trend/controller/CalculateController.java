package org.ozm.trend.controller;

import org.ozm.trend.pojo.IndexData;
import org.ozm.trend.pojo.Profit;
import org.ozm.trend.service.BackTestService;
import org.ozm.trend.service.CalculateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class CalculateController {

    @Autowired
    BackTestService backTestService;

    @Autowired
    CalculateService calculateService;
    //通过当日收盘价、ma、购买阈值和出售阈值预测对应code指数的收益
    @GetMapping("/profitCalculate/{cash}/{code}/{ma}/{buyThreshold}/{sellThreshold}/{closePoint}")
    @CrossOrigin
    public Map<String,Object> result(
            @PathVariable("cash")float cash,
            @PathVariable("ma")int ma,
            @PathVariable("buyThreshold")float buyThreshold,
            @PathVariable("sellThreshold")float sellThreshold,
            @PathVariable("closePoint")float closePoint,
            @PathVariable("closePoint")String date
    ){
       //将输入的closePoint，date封装为一个IndexData对象，然后对该对象进行买入卖出计算预测利润,返回Profit对象
        IndexData indexData = new IndexData();
        indexData.setClosePoint(closePoint);
        indexData.setDate(date);
        Map<String,Object> map = calculateService.result(indexData,cash,ma,buyThreshold,sellThreshold,closePoint);

        return map;
    }

}
