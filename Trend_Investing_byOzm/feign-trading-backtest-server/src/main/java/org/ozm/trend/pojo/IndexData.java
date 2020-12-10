package org.ozm.trend.pojo;
//指数
public class IndexData {
    String date; //日期
    float closePoint; //收盘价
    public String getDate() {
        return date;
    }
    public void setDate(String date) {
        this.date = date;
    }
    public float getClosePoint() {
        return closePoint;
    }
    public void setClosePoint(float closePoint) {
        this.closePoint = closePoint;
    }
}
