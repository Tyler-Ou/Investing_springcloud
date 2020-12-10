package org.ozm.trend.service;

import org.ozm.trend.dao.ExampleDao;
import org.ozm.trend.pojo.Example;
import org.ozm.trend.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExampleService {
    @Autowired
    ExampleDao exampleDao;


    //获得对应user - id的example项
    public List<Example> get(User user){
        return  exampleDao.findByUserOrderById(user);
    }


    public void save(Example example){
        exampleDao.save(example);
    }

    //通过对应user和id获取对应的Example
    public Example getI(User user,int id){
        return exampleDao.findByUserAndId(user,id);
    }

    //通过对应的user和code获取对应的Example
    public Example getII(User user,String code){
        return exampleDao.findByUserAndCode(user,code);
    }



}
