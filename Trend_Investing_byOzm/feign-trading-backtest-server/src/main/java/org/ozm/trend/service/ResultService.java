package org.ozm.trend.service;

import org.ozm.trend.client.IndexDataClient;
import org.ozm.trend.pojo.IndexData;
import org.ozm.trend.pojo.Profit;
import org.ozm.trend.pojo.Record;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//趋势投资拟合分析  趋势投资曲线分析
@Service
public class ResultService {

    //1、通过指数代码获得对应的指数数据
    //2、模拟不同的MA值、购入阈值、售出阈值的标准量 忽略手续费因素，默认初始资金为1000
    //3、利用指数数据 通过计算 获得对应标准量的 “交易统计” 的 数据
    //4、将数据全部封装返回
    //5、要返回利润画出当前标准量下趋势投资的曲线

    public Map<String,Object> result(int ma,float buyRate,float sellRate, List<IndexData> indexDatas){
        //设置分析预测ma值、购入售出阈值
        int analyse_ma = ma;
        float analyse_sellRate = sellRate;
        float analyse_buyRateList = buyRate;
        float initCash = 1000;
        float cash = initCash;
        float serviceCharge = 0; //忽略手续费因素
        float increase_rate; //增长率用于与购入阈值作对比
        float decrease_rate; //下降率用于与售出阈值作对比
        float share = 0; //份数。对于本金来说，能买股票的份数
        float value = 0; //价值，即通过本金购买的股票一共的价值
        float init = 0; //趋势投资线的纵坐标初始点

        int winCount = 0;//盈利次数
        float totalWinRate = 0;//盈利率
        float avgWinRate = 0;//平均盈利
        float totalLossRate = 0;
        int lossCount = 0;//亏损次数
        float avgLossRate = 0;//平均亏损

        //交易记录，用于买出和卖出时进行记录
        List<Record> records = new ArrayList<>();
        if(!indexDatas.isEmpty())  //一般与指数线始点保持一致
            init = indexDatas.get(0).getClosePoint();

        for(int i =0;i<indexDatas.size();i++){
            IndexData indexData = indexDatas.get(i);
            //记录当日收盘价
            float closePoint = indexData.getClosePoint();
            //同样地使用相同的购入逻辑和卖出逻辑获得对应数据
            float avg = getMA(i, ma, indexDatas);
            //1、以均值和当日收盘价作对比，获得当日的增长率（潜力）
            if (avg!=0){
                increase_rate = closePoint/avg;
            }else{
                increase_rate =0;
            }
            //2、算出前ma均线的最大值与当日收盘价作对比，获得当日的下降率(潜力)
            float max = getMax(i, ma, indexDatas);
            if (max!=0){
                decrease_rate = closePoint/max;
            }else{
                decrease_rate = 0;
            }

            //3、增长率、下降率分别于购入阈值和卖出阈值相比较
            if (avg!=0){
                if (increase_rate > buyRate){
                    if (0 == share) {
                        //买入
                        share = cash / closePoint;
                        cash = 0;
                        //将买入添加到记录当中
                        Record record = new Record();
                        record.setBuyClosePoint(indexData.getClosePoint());
                        record.setBuyDate(indexData.getDate());
                        record.setSellDate("n/a");
                        record.setSellClosePoint(0);
                        records.add(record);
                    }
                }else if (decrease_rate < sellRate){
                    //卖出 全卖原则   //如果没卖
                    if (0 != share) {
                        cash = share * closePoint * (1 - serviceCharge);
                        share = 0;
                        //将股票卖出后统计交易记录 交易记录统计 统计盈利次数和盈利率
                        Record record = records.get(records.size() - 1);
                        record.setSellDate(indexData.getDate());
                        record.setSellClosePoint(indexData.getClosePoint());
                        //本金盈亏比例
                        record.setRate(cash / initCash);
                        //将股票卖出后统计交易记录 交易记录统计 统计盈利次数和盈利率
                        //System.out.println("i="+i);
                        if (record.getSellClosePoint() - record.getBuyClosePoint() >0){
                            totalWinRate += (record.getSellClosePoint()-record.getBuyClosePoint())/record.getBuyClosePoint();
                            winCount++;
                        }else{ //2、统计亏损次数和亏损率
                            totalLossRate +=(record.getSellClosePoint()-record.getBuyClosePoint())/record.getBuyClosePoint();
                            lossCount++;
                        }
                    }
                }else{}
            }
            if (share!=0){
                value = share*closePoint;
            }else{
                value = cash;
            }

            //计算盈亏比例
            float rate = value / initCash;
        }
        //交易结束后 计算平均盈利或者亏损利率
        avgWinRate = totalWinRate / winCount;
        avgLossRate = totalLossRate / lossCount;
        Map<String,Object> map = new HashMap<>();
        map.put("winCount", winCount);//胜率
        map.put("lossCount", lossCount);//亏损率
        map.put("avgWinRate", avgWinRate);//平均盈利率
        map.put("avgLossRate", avgLossRate);//平均亏损率
        return map;
    }

    //获得对应均线的均值
    private float getMA(int i,int ma,List<IndexData> indexDatas){
        //1、判断其indexData是否存在有对应均线日的数据
        int before_start = i-1-ma; //前均日的第一天
        int before_end = i - 1; //前均日的最后一天
        float total_closePoint = 0;//合计收盘价
        if(before_start<0){
            return 0;
        }
        //2、从第一天到最后一天 历遍前均日的indexData 获取对应收盘价数据然后合计
        for(int v=before_start;v<before_end;v++){
            IndexData indexData_cmp = indexDatas.get(v);
            total_closePoint += indexData_cmp.getClosePoint();
        }

        //3、得出ma天的均值
        float avg = total_closePoint/(before_end-before_start);
        return avg;
    }
    //获得对应均线日最大值
    private static float getMax(int i, int ma, List<IndexData> list) {
        int before_start = i-1-ma; //前ma天的第一天

        if(before_start<0)
            before_start = 0;

        float max = 0;
        int  before_end= i-1;//前ma天的最后第一天
        for(int v = before_start;v<before_end;v++){
            //对比收盘价，筛选出最高的收盘价
            IndexData temp = list.get(v);
            if (max<temp.getClosePoint()){
                max = temp.getClosePoint();
            }
        }
        return max;
    }

    //通过交易记录 统计盈亏数据
/*    private void  trade_One(Record record,float totalWinRate,float winCount,float totalLossRate,float lossCount){
        //1、统计盈利次数和盈利率 如果卖出价格高于买入价格则盈利
        if (record.getSellClosePoint() - record.getBuyClosePoint() >0){
            totalWinRate += (record.getSellClosePoint()-record.getBuyClosePoint())/record.getBuyClosePoint();
            winCount++;
        }else{ //2、统计亏损次数和亏损率
            totalLossRate +=(record.getSellClosePoint()-record.getBuyClosePoint())/record.getBuyClosePoint();
            lossCount++;
        }
    }*/

//    public void trade_Two(float avgWinRate,float avgLossRate,float totalWinRate,float winCount,float totalLossRate,float lossCount){
//        //计算方式：盈利率/盈利次数
//        avgWinRate = totalWinRate / winCount;
//        avgLossRate = totalLossRate / lossCount;
//
//    }


}
