package org.ozm.trend.pojo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.*;

@Entity
@Table(name = "example_data")
@JsonIgnoreProperties({ "handler","hibernateLazyInitializer" })
public class ExampleData {

    @Id //这是一个主键
    @GeneratedValue(strategy = GenerationType.IDENTITY)//自增主键
    @Column(name="id")
    private Integer id;

    @Column(name = "date")
    private String date;
    @Column(name = "closePoint")
    private float closePoint;

    //这里对应Example~id
    @Column(name = "eid")
    private int eid;


    public Integer getId() {
        return id;
    }

    public String getDate() {
        return date;
    }

    public float getClosePoint() {
        return closePoint;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setClosePoint(float closePoint) {
        this.closePoint = closePoint;
    }
}
