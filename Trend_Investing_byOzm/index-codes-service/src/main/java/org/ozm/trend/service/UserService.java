package org.ozm.trend.service;

import org.ozm.trend.dao.UserDao;
import org.ozm.trend.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired
    UserDao userDao;

    public User get(String name){
        return userDao.findUserByName(name);
    }

}
