package com.lighting.routing.managment.controller;

import com.lighting.routing.managment.entity.Routing;
import com.lighting.routing.managment.service.IRoutingYamlEditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

/**
 * Gateway路由管理
 */
@RestController
@RequestMapping("/gateway/routing")
public class RoutingController {
    @Autowired
    private IRoutingYamlEditService routingYamlEditServiceImpl;

    /**
     * 获取路由列表
     *
     * @return
     */
    @GetMapping("/getRoutingList")
    public List<Object> getRoutingList() {
        return routingYamlEditServiceImpl.getRoutingList();
    }

    /**
     * 根据路由名称删除路由
     */
    @DeleteMapping("/removeRouting")
    public void removeRouting() {

    }

    /**
     * 添加和修改路由
     */

    @PostMapping("/addRouting")
    public void addRouting(Routing routing) throws IOException, URISyntaxException {
        routingYamlEditServiceImpl.addRouting(routing);
    }
}
