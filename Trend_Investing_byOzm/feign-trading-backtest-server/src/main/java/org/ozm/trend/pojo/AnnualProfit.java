package org.ozm.trend.pojo;
//每年收益实体类 用于作收益分布对比图
public class AnnualProfit {
    private int year;//年份
    private float indexIncome;//指数收益
    private float trendIncome;//趋势收益
    public int getYear() {
        return year;
    }
    public void setYear(int year) {
        this.year = year;
    }
    public float getIndexIncome() {
        return indexIncome;
    }
    public void setIndexIncome(float indexIncome) {
        this.indexIncome = indexIncome;
    }
    public float getTrendIncome() {
        return trendIncome;
    }
    public void setTrendIncome(float trendIncome) {
        this.trendIncome = trendIncome;
    }
    @Override
    public String toString() {
        return "AnnualProfit [year=" + year + ", indexIncome=" + indexIncome + ", trendIncome=" + trendIncome + "]";
    }
}
