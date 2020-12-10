package org.ozm.trend.pojo;

import java.io.Serializable;

//指数代码类，用于指数里的名称和代码 指数 = 多个股票形成的集合  如深沪指数 = 深沪股市中最优的500家公司的股票
//该类序列化是为了放入list中
public class Index implements Serializable {
    String code; //指数代码
    String name; //指数名称
    public String getCode() {
        return code;
    }
    public void setCode(String code) {
        this.code = code;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
}
