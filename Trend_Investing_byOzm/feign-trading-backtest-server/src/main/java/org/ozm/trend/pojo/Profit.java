package org.ozm.trend.pojo;
//预测利润类 方便与 IndexData 做对比。 趋势投资线就是拿利润类去构成的
public class Profit {
    String date;
    float value;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }


}
