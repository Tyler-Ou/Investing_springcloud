package org.ozm.trend.controller;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import org.ozm.trend.pojo.*;
import org.ozm.trend.service.BackTestService;
import org.ozm.trend.service.ExampleService;
import org.ozm.trend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import zipkin2.internal.gson.stream.JsonReader;

import javax.xml.crypto.Data;
import java.io.*;
import java.util.*;

@RestController
public class BackTestController {
    @Autowired
    BackTestService backTestService;

    @Autowired
    StringRedisTemplate stringRedisTemplate;
    @Autowired
    ExampleService exampleService;

    @Autowired
    UserService userService;




    //////////////////////////未添加日期控件前的逻辑////////////////////////////////////

    /*
        这次服务的过程是通过跨域的方式实现的，这里是80xx端口到微服务的8021....端口
        @GetMapping("/simulate/{code}")
        @CrossOrigin
        public Map<String,Object> backTest(@PathVariable("code") String code) throws Exception{
            List<IndexData> allIndexDatas = backTestService.indexDataList(code);
            Map<String,Object> result = new HashMap<>();
            result.put("indexDatas", allIndexDatas); //以indexDatas键来保存所有的指数数据(不是指数代码)
            return result;
        }
    */
    //////////////////////////未添加日期控件前的逻辑////////////////////////////////////



    //////////////////////////添加日期控件后的逻辑////////////////////////////////////
    //日期功能需求
    //1、当获取到指数数据时，将对应开始的日期和结束日期获取到并显示在日期控件上
    //2、每次切换指数代码，其代码对应的指数数据再对应的开始日期和结束日期也随之更新
    //3、日期控件可选择查看日期范围内的某一时期数据
    //4、可查看的日期范围，从开始日期到结束日期为止
    //5、对于服务端，如果没有提供开始和结束日期，则返回所有数据。 如果提供了，则返回指定日期范围的对应数据。
    //6、开始日期不能大于结束日期

    @GetMapping("/simulate/{code}/{ma}/{buyThreshold}/{sellThreshold}/{serviceCharge}/{startDate}/{endDate}/{dataType}")
    @CrossOrigin
    public Map<String,Object> backTest(
            @PathVariable("code") String code,
            @PathVariable("ma") int ma,
            @PathVariable("buyThreshold")float buyThreshold,
            @PathVariable("sellThreshold")float sellThreshold,
            @PathVariable("startDate") String startDate,
            @PathVariable("serviceCharge")float sCharge,
            @PathVariable("endDate") String endData,
            @PathVariable("dataType")String dataType
    )throws Exception{
        //需求一：通过fegin client在index-data-server 获取对应的指数数据
        List<IndexData> allIndexDatas = new ArrayList<>();
//        @PathVariable("root")String root
        if ("public".equals(dataType)){
            //如果选择的指数为公共实例的指数则从fegin-client中获取数据
            allIndexDatas = backTestService.indexDataList(code);
        }else if ("root".equals(dataType)){
            //如果选择查看的指数为用户导入的指数，则从文件中获取数据
            allIndexDatas = selectUser(code);
        }
//       需要自定义错误
        if (allIndexDatas==null){
            Map<String,Object> err =  new HashMap<>();
            return (Map<String, Object>) err.put("Errmsg","数据上传有误,请尝试删除后重新上传数据");

        }

        //System.out.println(allIndexDatas.size());
        //allIndexDatas = backTestService.indexDataList(code);

        //需求一：通过指数数据获取其对应的开始日期和最后日期
        String indexStartDate = allIndexDatas.get(0).getDate();
        String indexEndDate = allIndexDatas.get(allIndexDatas.size()-1).getDate();

        //在日期的层面上校验输入的开始日期和结束日期是否合法，同时确定indexData对象
        // 开始日期和结束日期的范围内的日期是否合法

        //传入的日期参数只用于取限定范围的数据，如果不传入则去全部数据
        allIndexDatas = filterByDateRange(allIndexDatas,startDate,endData);

        //虚拟数据模拟趋势投资收益曲线

        //购买与卖出阈值
        float sellRate = sellThreshold;
        float buyRate = buyThreshold;
        float serviceCharge = sCharge;
        Map<String,?> simulateResult= backTestService.simulate(ma,sellRate, buyRate,serviceCharge, allIndexDatas);
        List<Profit> profits = (List<Profit>) simulateResult.get("profits");
        List<Record> records = (List<Record>) simulateResult.get("records");

        //收益一览
        //1、获取指数曲线和趋势投资曲线总年份
        float years = backTestService.getYear(allIndexDatas);
        //2、计算指数的收益和趋势投资的收益，以及对应的年化收益率
        //2.1、指数收益：最后收盘价-最初收盘价/最初收盘价
        float lastClosePoint = allIndexDatas.get(allIndexDatas.size() - 1).getClosePoint();
        float beginClosePonit = allIndexDatas.get(0).getClosePoint();
        float indexProfit = (lastClosePoint - beginClosePonit) /beginClosePonit;
        //2.2、指数年化收益
        float indexYearsProfit = (float) Math.pow(1+indexProfit, 1/years) - 1;
        //3.1、趋势投资收益 最后收益-最初收益/最初收益
        float lastTrendProfit = profits.get(profits.size() -1).getValue();
        float beginTrendProfit = profits.get(0).getValue();
        float trendProfit = (lastTrendProfit - beginTrendProfit)/beginTrendProfit;
        //3.2、趋势投资年化收益
        float trendYearsProfit  =  (float) Math.pow(1+trendProfit, 1/years) - 1;


        //交易记录
        int winCount = (Integer) simulateResult.get("winCount");
        int lossCount = (Integer) simulateResult.get("lossCount");
        float avgWinRate = (Float) simulateResult.get("avgWinRate");
        float avgLossRate = (Float) simulateResult.get("avgLossRate");


        //收益分布
        List<AnnualProfit> annualProfits = (List<AnnualProfit>) simulateResult.get("annualProfits");


        //将List<IndexData再打包>
        Map<String,Object> result =  new HashMap<>();
        result.put("indexDatas", allIndexDatas);//合法的IndexData数据集
        result.put("indexStartDate", indexStartDate);//开始日期
        result.put("indexEndDate", indexEndDate);//结束日期
        result.put("profits", profits); //收益
        result.put("records", records); //交易记录
        //返回收益一览
        result.put("years", years);//年份
        result.put("indexProfit", indexProfit);//指数收益
        result.put("indexYearsProfit", indexYearsProfit); //指数年化收益
        result.put("trendProfit", trendProfit); //趋势投资收益
        result.put("trendYearsProfit", trendYearsProfit); //趋势投资年化收益

        //返回交易记录
        result.put("winCount", winCount);
        result.put("lossCount", lossCount);
        result.put("avgWinRate", avgWinRate);
        result.put("avgLossRate", avgLossRate);

        //返回收益分布结果
        result.put("annualProfits", annualProfits);







        return result;
    }

    public List<IndexData> filterByDateRange(List<IndexData> allIndexDatas,String startDate,String endDate){
        //日期的非空判断,其实即为，如果日期不指定，则返回所有的数据
        if (StrUtil.isBlankOrUndefined(startDate)|| StrUtil.isBlankOrUndefined(endDate)){
            return allIndexDatas;
        }

        List<IndexData> result = new ArrayList<>();

        //将字符串的开始于结束日期进行日期格式化
        Date startDate_format = DateUtil.parse(startDate);
        Date endDate_format = DateUtil.parse(endDate);

        //指数数据的日期都遍历出校验是否在开始日期和结束日期内
        for(IndexData indexData:allIndexDatas){
            Date date = DateUtil.parse(indexData.getDate());
            //确保时间都在开始和结束的范围内
            if (
                    date.getTime()>=startDate_format.getTime() &&
                    date.getTime()<=endDate_format.getTime()
            ){
              //校验无误后将该indexData原对象添加进List<IndexData>里，然后进行返回
                result.add(indexData);
            }
        }
        //返回带着合法的开始日期和结束日期，并且所有indexData对象的日期都在开始和结束范围内的list<indexData>
        return result;

    }

    public  List<IndexData> selectUser(String code) throws FileNotFoundException {
        //上传用于导入的数据。
        //逻辑：通过redis获取用户的对象，并通过用户对象找到对应的Example
        // 再从Example中筛选status为success和指定code的字段。获得其Example对象。
        //最后使用example - id、用户对象的name 的和RestTemplate在文件对应路径中获取对应用户name和id的照片
        String username = stringRedisTemplate.opsForValue().get("loginResponse");
        User user = userService.get(username);
        Example success_example =  new Example();
        List<IndexData> result = new ArrayList<>();
        if (user!=null){
            //对应用户并且status字段为success和对应code的example对象
            success_example = exampleService.getII(user,code);

            //导入的文件路径
            String fileFolder = ResourceUtils.getURL("classpath:").getPath()+"public/uploadFile/"+user.getUsername();
            //文件名
            String fileName = "/"+success_example.getId()+".json";
            //读取json文件内容
            File f = new File(fileFolder+fileName);

            try(
                    FileReader fileReader = new FileReader(f);
                    JsonReader jsonReader = new JsonReader(fileReader);
            )
            {

                jsonReader.beginArray();
                while (jsonReader.hasNext()){ //一组数据一组数据如此循环
                    jsonReader.beginObject();
                   IndexData indexData = new IndexData();
                    while (jsonReader.hasNext()){ //每一组数据中的每一个字段循环
                        String tag = jsonReader.nextName();
                        if ("date".equals(tag)){
                            String date = jsonReader.nextString();
                            indexData.setDate(date);
                        }else if ("closePoint".equals(tag)){
                            float closePoint = (float)jsonReader.nextDouble();
                            indexData.setClosePoint(closePoint);
                        }
                    }
                    result.add(indexData);
                    jsonReader.endObject();
                }
                jsonReader.endArray();
            }catch (IOException o){
                o.printStackTrace();
                return null;
            }

        }
        //对数据进行自然排序
        Collections.reverse(result);
            return  result;
    }






}
