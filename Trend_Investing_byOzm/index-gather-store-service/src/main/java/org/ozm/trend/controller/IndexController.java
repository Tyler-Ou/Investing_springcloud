package org.ozm.trend.controller;

import org.ozm.trend.pojo.Index;
import org.ozm.trend.service.IndexService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
//该控制器用于将数据返回前端
public class IndexController {
    @Autowired
    IndexService indexService;

    //  http://127.0.0.1:8001/getCodes
    //  http://127.0.0.1:8001/freshCodes
    //  http://127.0.0.1:8001/removeCodes

    //获取数据 第一次获取数据时由于是从redis上获取数据，如果没有数据即需要更新一次
    @GetMapping("/getCodes")
    public List<Index> get() throws Exception{
        return indexService.get();
    }

    //更新数据
    @GetMapping("/freshCodes")
    public List<Index> fresh() throws Exception {
        return indexService.fresh();
    }

    //删除数据
    @GetMapping("/removeCodes")
    public String remove() throws Exception {
        indexService.remove();
        return "remove codes successfully";
    }

}
