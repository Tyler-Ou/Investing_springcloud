package org.ozm.trend.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.*;

//用户创建的实例
@Entity
@Table(name="example")
@JsonIgnoreProperties({ "handler","hibernateLazyInitializer" })
public class Example {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    int id;

    @Column(name = "code")
    String code; //指数实例代码

    @Column(name = "name")
    String name; //指数实例名称


    @Column(name = "create_time")
    String create_time;

    @Column(name = "status")
    String status;


    //外键关联
    @ManyToOne
    @JoinColumn(name="uid")
    private User user;


    public String getStatus() {
        return status;
    }

    public String getCreate_time() {
        return create_time;
    }

    public int getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public User getUser() {
        return user;
    }



    public void setCreate_time(String create_time) {
        this.create_time = create_time;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
