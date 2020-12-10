package org.ozm.trend.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.*;

@Entity
@Table(name="user")
//该对象作为持久化对象，而无须json化。在前台访问后台进行交互时，对该持久化对象进行数据的自动注入
@JsonIgnoreProperties({ "handler","hibernateLazyInitializer" })
public class User {
    @Id //这是一个主键
    @GeneratedValue(strategy = GenerationType.IDENTITY)//自增主键
    private Integer id;

    @Column
    private String name;
    @Column
    private String password;
    @Column
    private String salt;


    public void setId(Integer id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.name = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public Integer getId() {
        return id;
    }

    public String getUsername() {
        return name;
    }

    public String getPassword() {
        return password;
    }

    public String getSalt() {
        return salt;
    }
}
