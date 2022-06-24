package com.lighting.routing.managment.service.impl;

import com.lighting.routing.config.DynamicRouteServiceImpl;
import com.lighting.routing.managment.entity.Routing;
import com.lighting.routing.managment.service.IRoutingYamlEditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.FilterDefinition;
import org.springframework.cloud.gateway.handler.predicate.PredicateDefinition;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import javax.annotation.PostConstruct;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * GateWay路由配置界面
 */
@Service
public class RoutingYamlEditServiceImpl implements IRoutingYamlEditService {
    @Autowired
    private DynamicRouteServiceImpl dynamicRouteServiceImpl;

    private Map<String, Object> ret;

    /**
     * 初始化读取路由文件到缓存中
     */
    @PostConstruct
    public void readRoutingYamlResource() {
        Yaml yaml = new Yaml();
        ret = (Map<String, Object>) yaml.load(this.getClass().getClassLoader()
                .getResourceAsStream("application.yml"));
    }

    /**
     * 获取RoutingList
     *
     * @return
     */
    @Override
    public List<Object> getRoutingList() {
        List<Object> routingList = (List<Object>) getValue(ret, "spring.cloud.gateway.routes");
        return routingList;
    }

    @Override
    public void addRouting(Routing routing) throws IOException, URISyntaxException {
        List<Object> routingList = getRoutingList();
        for (Object thRouting : routingList) {
            if (((Map) thRouting).get("id").equals(routing.getId())) {
                routingList.remove(thRouting);
                break;
            }
        }

        //转换路由信息
        Map<String, Object> routMap = new HashMap<String, Object>();
        routMap.put("id", routing.getId());
        routMap.put("uri", routing.getUri());
        routMap.put("predicates", routing.getPredicates());
        routMap.put("filters", routing.getFilters());
        routingList.add(routMap);
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);//通常使用的yaml格式
        options.setDefaultScalarStyle(DumperOptions.ScalarStyle.PLAIN);//标量格式


        Yaml yaml = new Yaml(options);
        FileWriter fileWriter = new FileWriter("application.yml");
        yaml.dump(ret, fileWriter);
        fileWriter.close();

        RouteDefinition routeDefinition = new RouteDefinition();
        routeDefinition.setUri(new URI(routing.getUri()));
        routing.getFilters().forEach(filter -> {
            routeDefinition.getFilters().add(new FilterDefinition(filter));
        });
        routing.getPredicates().forEach(predicate -> {
            routeDefinition.getPredicates().add(new PredicateDefinition(predicate));
        });
        routeDefinition.setId(routing.getId());

        dynamicRouteServiceImpl.addAndRefreshRouteDefinition(routeDefinition);
    }

    private Object getValue(Map<String, Object> ret, String key) {
        return getValue(ret, key.split("\\."), 0);
    }

    private Object getValue(Map<String, Object> ret, String[] keys, int i) {
        if (keys.length == (i + 1)) {
            return ret.get(keys[i]);
        } else {
            return getValue((Map<String, Object>) ret.get(keys[i]), keys, i + 1);
        }
    }


}
