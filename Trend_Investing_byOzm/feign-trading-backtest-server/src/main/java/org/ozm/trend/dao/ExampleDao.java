package org.ozm.trend.dao;

import org.ozm.trend.pojo.Example;
import org.ozm.trend.pojo.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExampleDao extends JpaRepository<Example,Integer> {
    List<Example> findByUserOrderById(User user);
    Example findByUserAndId(User user,int id);
    Example findByUserAndCode(User user,String code);
}
