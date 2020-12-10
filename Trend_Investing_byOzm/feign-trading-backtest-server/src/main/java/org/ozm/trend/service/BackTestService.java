package org.ozm.trend.service;


import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import org.ozm.trend.client.IndexDataClient;
import org.ozm.trend.pojo.AnnualProfit;
import org.ozm.trend.pojo.IndexData;
import org.ozm.trend.pojo.Profit;
import org.ozm.trend.pojo.Record;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

//service用来提供回测服务，client用来调用数据微服务
@Service
public class BackTestService {

//    踩坑：切勿写在此处，因为Service的生命周期未结束前，这里的变量会叠加
//    //用于交易统计的变量
//    int winCount = 0;//盈利次数
//    float totalWinRate = 0;//
//    float avgWinRate = 0;//平均盈利
//    float totalLossRate = 0;
//    int lossCount = 0;//亏损次数
//    float avgLossRate = 0;//平均亏损

    @Resource
    IndexDataClient indexDataClientas;

    //通过回测服务去调用feign client，而feign client去获取微服务，最终得到微服务的数据
    public List<IndexData> indexDataList(String code){
        //回测服务通过feign client去获取数据
        List<IndexData> result = indexDataClientas.getIndexData(code);
        //对数据进行自然排序
        Collections.reverse(result);
        //从控制台上打印出来
//        for (IndexData indexData : result) {
//            System.out.println(indexData.getDate());
//        }
        return result;
    }

    /*生成与指数对比的趋势曲线，即pojo.indexDatas与pojo.Profit对比
        购入逻辑：1、取某个点的 “当日” 收盘价，与前XX天的MA值“平均增长均值”进行对比，
                    这样可以得到 “当日” 增长率，最后用 “当日” 增长率 与 标准阀值进行比较，
                    如果高于阀值那就表明， “明日” 就有上升的趋势，之后就可以进行购入了
                 2、那么高于多少的标准呢？有一个参考标准就是购入阈值，一
                    假设购入阀值为是101%，那么当“当日” 增长率大于或等于购入阀值时，
                    那么可以认为改股是值得购买的。


        卖出逻辑：1、取某个点的单日收盘价，与取MA “最高点” 值进行对比，得到 “当日”下降率
                    最后用 “当日”下降率 与 标准阀值进行比较，如果 “当日”下降率 低于标准阀值
                    那么可以说下一点就有下降的趋势。
                 2、那么低于多少的标准呢？ 有一个参考标准就是卖出阈值，
                 一般来说当日收盘价比取MA “最高点”值还要低于或等于95%，即说卖出阈值是95%，是值得卖出的、
                 3、阈值参考标准：日收盘价低于MA值 95%

        参数解释：
                1、ma即为ma曲线moving average
                2、sellRate 出售阈值 阈值用来判断某个点是否指定购买
                3、buyRate  购买阈值
                4、serviceCharge 每次买入卖出的手续费
                5、indexDatas 用于回测服务
    */
    public Map<String,Object> simulate(int ma, float sellRate, float buyRate, float serviceCharge, List<IndexData> indexDatas){
        //趋势投资收益图
        List<Profit> profits =new ArrayList<>();
        //交易记录，用于买出和卖出时进行记录
        List<Record> records = new ArrayList<>();



        //用于交易统计的变量
        int winCount = 0;//盈利次数
        float totalWinRate = 0;//
        float avgWinRate = 0;//平均盈利
        float totalLossRate = 0;
        int lossCount = 0;//亏损次数
        float avgLossRate = 0;//平均亏损


        float initCash = 1000;//初始本金
        float cash = initCash;
        float share = 0; //份数。对于本金来说，能买股票的份数
        float value = 0; //价值，即通过本金购买的股票一共的价值






        float init = 0; //趋势投资线的纵坐标初始点

        if(!indexDatas.isEmpty())  //一般与指数线始点保持一致
            init = indexDatas.get(0).getClosePoint();

        for (int i =0;i<indexDatas.size();i++) {
            IndexData indexData = indexDatas.get(i);
            //当日收盘价
            float closePoint = indexData.getClosePoint();
            //计算“前ma天” 合计的 “平均增长均值”
            float avg = getMA(i, ma, indexDatas);
            float increase_rate;
            //用当日收盘价与ma天收盘价均值作对比，得出当日的增长率
            if (avg!=0){
               increase_rate = closePoint / avg;
            }else{
                increase_rate = 0;
            }

            float decrease_rate;
            //计算“前ma天” 的最高收盘价
            float max = getMax(i, ma, indexDatas);
            //用当日收盘价 与“前ma天” 的最高收盘价作比对，得出下降率
            if (max!=0){
                decrease_rate = closePoint / max;
            }else{
                decrease_rate = 0;
            }

            //如果当日增长率高于或等于购入阈值，即当日已经高于MA线，进行购入相关操作
            if (avg != 0) { //如果没前ma天数据，不作购入操作
                if (increase_rate > buyRate) {
                    //进行购入操作时，查看原来是否已经购买了
                    if (0 == share) {
                        //用本金全买
                        share = cash / closePoint;
                        cash = 0;

                        //添加进交易明细
                        Record record = new Record();
                        record.setBuyDate(indexData.getDate());
                        record.setBuyClosePoint(indexData.getClosePoint());
                        record.setSellDate("n/a");
                        record.setSellClosePoint(0);
                        records.add(record);
                    }
                } else if (decrease_rate < sellRate) {
                    //如果没卖
                    if (0 != share) {
                        //全卖
                        cash = share * closePoint * (1 - serviceCharge);
                        share = 0;
                        //卖出时添加到交易明细 出售的时候，修改前面创建的这个交易对象
                        Record record =records.get(records.size()-1);
                        record.setSellDate(indexData.getDate());
                        record.setSellClosePoint(indexData.getClosePoint());
                        float rate = cash / initCash;
                        //本金盈亏比例
                        record.setRate(rate);
                       // System.out.println("i="+i);
                        //将股票卖出后统计交易记录 交易记录统计 统计盈利次数和盈利率
                        //Record record,float totalWinRate,float winCount,float totalLossRate,float lossCount
                        if (record.getSellClosePoint()-record.getBuyClosePoint()>0){
                            //盈利率计算为，每次 卖出与买入的差价/买入时原收盘价
                            totalWinRate +=(record.getSellClosePoint()-record.getBuyClosePoint())/record.getBuyClosePoint();
                            winCount++;
                        }else {//当该条记录的买入价格高于卖出价格即为亏损，统计亏损次数和亏损率
                            totalLossRate +=(record.getSellClosePoint()-record.getBuyClosePoint())/record.getBuyClosePoint();
                            lossCount++;
                        }
                    }
                }else{}
            }
            //当完成判断后，可以得出要么当前有存货Share要么当前有现金cash。以此推出目前价值
            if (share != 0) {
                value = closePoint * share; //当前的价值
            } else {
                value = cash;
            }
            //计算盈亏比例
            float rate = value / initCash;

            //计算预测利润
            Profit profit = new Profit();
            profit.setValue(rate * init);
            profit.setDate(indexData.getDate());
            //将该对象注入profits内，多个profit即可形成一条趋势投资收益线
            profits.add(profit);
        }
        //交易结束后 计算平均盈利或者亏损利率
        //float avgWinRate,float avgLossRate,float totalWinRate,float winCount,float totalLossRate,float lossCount
        //计算方式：盈利率/盈利次数
        avgWinRate = totalWinRate / winCount;
        avgLossRate = totalLossRate / lossCount;
        List<AnnualProfit> annualProfits = caculateAnnualProfits(indexDatas, profits);
        Map<String,Object> map = new HashMap<>();
        map.put("profits", profits);
        map.put("records", records);

        //放入交易记录 的 盈利次数 、亏损次数、平均盈利率 = 盈利率/盈利次数  平均亏损率 =亏损率/亏损次数
        map.put("winCount", winCount);
        map.put("lossCount", lossCount);
        map.put("avgWinRate", avgWinRate);
        map.put("avgLossRate", avgLossRate);

        //收益对比
        map.put("annualProfits", annualProfits);
        return map;
    }

    //获得MA均线中的最大值
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

    //均线平均值统计
    private float getMA(int i,int ma,List<IndexData> indexDatas){
        //当日时间 用于保留写法，比对时间
        //Date now_time = DateUtil.parse(indexDatas.get(i).getDate());
        //前ma天的 合计收盘价
        float before_closePoint = 0;
        int before_start = i-1-ma; //前ma天的第一天
        if (before_start<0){ //当第i天时，没前ma天的数据项时退出
            return 0;
        }
        int  before_end= i-1;//前ma天的最后第一天
        for (int v =before_start;v<before_end;v++){
            IndexData indexData_cmp = indexDatas.get(v);
            //数据天数连续时，算出合计收盘价
            before_closePoint+=indexData_cmp.getClosePoint();
            //保留写法，当数据天数不连续时,需要比对时间
            // Date before_time = DateUtil.parse(indexData_cmp.getDate());
            //校验当日时间前ma天是否存在，如果存在记录其ma的当日收盘价
//            if (now_time.getTime()>before_time.getTime()){
//                before_closePoint+=indexData_cmp.getClosePoint();
//            }else{
//               return 0;
//            }
        }
        //得出ma天的平均值
        float avg = before_closePoint/(before_end-before_start);
        return  avg;
    }

    //获得投资年份
    public float getYear(List<IndexData> allIndexDates){
        float year;
        //从获取所有日期的数据，并统计共经历多少年
        float years;
        String sDateStart = allIndexDates.get(0).getDate();
        String sDateEnd = allIndexDates.get(allIndexDates.size() - 1).getDate();

        //要将获取的字符串格式的时间转换为Date数据类型以比对
        Date dateStart = DateUtil.parse(sDateStart);
        Date dateEnd = DateUtil.parse(sDateEnd);

        long days = DateUtil.between(dateStart,dateEnd, DateUnit.DAY);
        //以年为单位
        years = days/365f;
        return years;
    }


    //交易记录统计 统计盈利次数和盈利率
    private void trade_One(Record record,float totalWinRate,float winCount,float totalLossRate,float lossCount){
        //当该条记录的卖出价格高于买入价格即为盈利，统计盈利次数和盈利率
        if (record.getSellClosePoint()-record.getBuyClosePoint()>0){
            //盈利率计算为，每次 卖出与买入的差价/买入时原收盘价
            totalWinRate +=(record.getSellClosePoint()-record.getBuyClosePoint())/record.getBuyClosePoint();
            winCount++;
        }else {//当该条记录的买入价格高于卖出价格即为亏损，统计亏损次数和亏损率
            totalLossRate +=(record.getSellClosePoint()-record.getBuyClosePoint())/record.getBuyClosePoint();
            lossCount++;
        }
    }

    //交易结束后 计算平均盈利或者亏损利率
//    private void trade_Two(float avgWinRate,float avgLossRate,float totalWinRate,float winCount,float totalLossRate,float lossCount){
//        //计算方式：盈利率/盈利次数
//        avgWinRate = totalWinRate / winCount;
//        avgLossRate = totalLossRate / lossCount;
//
//    }


    //计算完整时间范围内，每一年的指数投资收益和趋势投资收益
    public List<AnnualProfit> caculateAnnualProfits(List<IndexData> indexDatas, List<Profit> profits){
        List<AnnualProfit> result = new ArrayList<>();
        String strStartDate = indexDatas.get(0).getDate();
        String strEndDate = indexDatas.get(indexDatas.size()-1).getDate();
        Date startDate = DateUtil.parse(strStartDate);
        Date endDate = DateUtil.parse(strEndDate);
        int startYear = DateUtil.year(startDate);
        int endYear = DateUtil.year(endDate);
        for (int year = startYear;year<=endYear;year++){
            AnnualProfit annualProfit = new AnnualProfit();
            annualProfit.setYear(year);
            float indexIncome = getIndexIncome(year,indexDatas);
            float trendIncome = getTrendIncome(year,profits);
            annualProfit.setIndexIncome(indexIncome);
            annualProfit.setTrendIncome(trendIncome);
            result.add(annualProfit);
        }
        return result;
    }





    //计算某一年的指数收益
    private float getIndexIncome(int year,List<IndexData> indexDatas){
        IndexData first = null;
        IndexData last = null;
        for(IndexData indexData:indexDatas){
            String strDate = indexData.getDate();
            //获得年份字段的年
            int currentYear = getiYear(strDate);
            if(currentYear==year){
                if (null==first){
                    first = indexData;
                }
                last = indexData;
            }
        }
        return (last.getClosePoint()-first.getClosePoint())/first.getClosePoint();
    }


    //计算某一年趋势投资收益
    private float getTrendIncome(int year,List<Profit> profits){
        Profit first = null;
        Profit last = null;

        for (Profit profit:profits){
            String strDate = profit.getDate();
            int currentYear = getiYear(strDate);
            if (currentYear == year){
                if (null==first){
                    first = profit;
                }
                last = profit;
            }
            if(currentYear > year)
                break;
        }
        return (last.getValue() - first.getValue())/first.getValue();
    }




    //获取年份日期字段的年份
    private int getiYear(String date){
        String strYear= StrUtil.subBefore(date, "-", false);
        return Convert.toInt(strYear);
    }






}
