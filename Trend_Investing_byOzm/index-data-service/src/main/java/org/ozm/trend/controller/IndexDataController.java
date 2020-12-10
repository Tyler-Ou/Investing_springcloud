package org.ozm.trend.controller;

import org.ozm.trend.config.IpConfiguration;
import org.ozm.trend.pojo.IndexData;
import org.ozm.trend.server.IndexDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class IndexDataController {
    //http://127.0.0.1:8021/data/000300
    @Autowired
    IndexDataService indexDataService;
    @Autowired
    IpConfiguration ipConfiguration;

    @GetMapping("/data/{code}")
    public List<IndexData> get(@PathVariable("code") String code) throws Exception{
        System.out.println("current instance is :" + ipConfiguration.getPort());
        return indexDataService.get(code);
    }


}
