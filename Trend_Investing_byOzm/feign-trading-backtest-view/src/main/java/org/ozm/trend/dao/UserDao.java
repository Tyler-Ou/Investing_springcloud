package org.ozm.trend.dao;

import org.ozm.trend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserDao extends JpaRepository<User,Integer> {

    //登录时通过账号和密码获取用户
    User getByNameAndPassword(String name, String password);
}
