package org.ozm.trend.controller;

import org.ozm.trend.pojo.Example;
import org.ozm.trend.pojo.IndexData;
import org.ozm.trend.pojo.Profit;
import org.ozm.trend.pojo.User;
import org.ozm.trend.service.BackTestService;
import org.ozm.trend.service.ExampleService;
import org.ozm.trend.service.ResultService;
import org.ozm.trend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import zipkin2.internal.gson.stream.JsonReader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

//交易统计拟合
@RestController
public class BackResultController {
    @Autowired
    BackTestService backTestService;
    @Autowired
    ResultService resultService;

    @Autowired
    StringRedisTemplate stringRedisTemplate;
    @Autowired
    ExampleService exampleService;

    @Autowired
    UserService userService;




    @GetMapping("/tradeResult/{code}/{ma}/{dataType}")
    public List<Object> result(
            @PathVariable("code")String code,
            @PathVariable("ma")int ma,
            @PathVariable("dataType")String dataType
            ) throws FileNotFoundException {
        //1、通过代码获得对应的指数数据

        List<IndexData> indexData = new ArrayList<>();
//        @PathVariable("root")String root
        if ("public".equals(dataType)){
            //如果选择的指数为公共实例的指数则从fegin-client中获取数据
            indexData = backTestService.indexDataList(code);
        }else if ("root".equals(dataType)){
            //如果选择查看的指数为用户导入的指数，则从文件中获取数据
            indexData = selectUser(code);
        }
        //System.out.println(indexData.size());

        //这里通过数据的拟合，考虑所有的组合因素
        //2、返回对应的数据合集
        //ma 预设分别为 5、10、20、60 默认为5
        //购入阈值分别为1.01-1.05 默认从1.01开始
        //卖出阈值分别为0.95-0.99
        //int[] analyse_ma = {5,10,20,60};
        int analyse_ma = ma;
        float[] analyse_buyRateList = {1.01f,1.02f,1.03f,1.04f,1.05f};
        float[] analyse_sellRateList = {0.95f,0.96f,0.97f,0.98f,0.99f};
        List resultDateList = new ArrayList();

        for(int i = 0;i<analyse_buyRateList.length;i++){
            for(int j = 0;j<analyse_sellRateList.length;j++){
                //从1.01开始组合搭配
                Map<String,Object> map = resultService.result(
                        analyse_ma,
                        analyse_buyRateList[i],
                        analyse_sellRateList[j],
                        indexData
                );
                //用于趋势投资曲线
                //List<Profit> profits = (List<Profit>) map.get("profits");
                //交易记录
                int winCount = (Integer) map.get("winCount");
                int lossCount = (Integer) map.get("lossCount");
                float avgWinRate = (Float) map.get("avgWinRate");
                float avgLossRate = (Float) map.get("avgLossRate");

                //添加到结果map中
                Map<String,Object> resultDate = new HashMap<>();
                //resultDate.put("profits",profits);
                resultDate.put("analyse_buyRate",analyse_buyRateList[i]);
                resultDate.put("analyse_sellRate",analyse_sellRateList[j]);
                resultDate.put("winCount", winCount);
                resultDate.put("lossCount", lossCount);
                resultDate.put("avgWinRate", avgWinRate);
                resultDate.put("avgLossRate", avgLossRate);
                //将结果放入list中
                resultDateList.add(resultDate);
            }

        }
        //System.out.println(resultDateList.size());
        return resultDateList;
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
            }

        }
        //对数据进行自然排序
        Collections.reverse(result);
        return  result;
    }



}
