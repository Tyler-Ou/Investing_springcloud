package org.ozm.trend.pojo;

import java.io.Serializable;

public class IndexData implements Serializable {

    String date;
    float closePoint;
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
