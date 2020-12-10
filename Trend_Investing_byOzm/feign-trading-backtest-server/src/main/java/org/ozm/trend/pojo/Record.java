package org.ozm.trend.pojo;
//transaction Record  交易记录 用于交易明细显示
public class Record {

    private String buyDate; //买入时间
    private String sellDate;//卖出时间
    private float buyClosePoint;//买入时收盘价
    private float sellClosePoint;//卖出时收盘价
    private float rate;//盈亏比例

    public String getBuyDate() {
        return buyDate;
    }
    public void setBuyDate(String buyDate) {
        this.buyDate = buyDate;
    }
    public String getSellDate() {
        return sellDate;
    }
    public void setSellDate(String sellDate) {
        this.sellDate = sellDate;
    }
    public float getBuyClosePoint() {
        return buyClosePoint;
    }
    public void setBuyClosePoint(float buyClosePoint) {
        this.buyClosePoint = buyClosePoint;
    }
    public float getSellClosePoint() {
        return sellClosePoint;
    }
    public void setSellClosePoint(float sellClosePoint) {
        this.sellClosePoint = sellClosePoint;
    }
    public float getRate() {
        return rate;
    }
    public void setRate(float rate) {
        this.rate = rate;
    }

}
