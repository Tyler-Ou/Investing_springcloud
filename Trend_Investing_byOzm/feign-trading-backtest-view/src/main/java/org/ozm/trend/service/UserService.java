package org.ozm.trend.service;

import org.ozm.trend.dao.UserDao;
import org.ozm.trend.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@CacheConfig(cacheNames="loginUser")
public class UserService {
    @Autowired
    UserDao userDao;

    @Autowired
    StringRedisTemplate redisTemplate;

    //校验数据库中是否存在对应user和password
    public User login(String username,String password)
    {
        return userDao.getByNameAndPassword(username,password);
    }

    //将校验完成的user对象存储在redis上
    @Cacheable(key = "'now_user'")
    public String redis_save(String username){
        return username;
    }

    //使用redisTemplate校验redis上是否存在校验完成的user,直接校验会出错，因为传入的数据类型是String
//    public boolean isExist(){
//        String key = "now_user";
//        System.out.println(redisTemplate);
//        return  redisTemplate.hasKey(key);
//    }


    //redisTemplate结果序列化



}
