package com.lighting.routing.managment.entity;

import io.swagger.annotations.ApiModelProperty;

import java.util.List;

/**
 * 路由配置
 */
public class Routing {
    @ApiModelProperty("路由id，唯一键")
    private String id;

    @ApiModelProperty("路由地址")
    private String uri;

    @ApiModelProperty("路由策略")
    private List<String> predicates;

    @ApiModelProperty("路由过滤")
    private List<String> filters;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public List<String> getPredicates() {
        return predicates;
    }

    public void setPredicates(List<String> predicates) {
        this.predicates = predicates;
    }

    public List<String> getFilters() {
        return filters;
    }

    public void setFilters(List<String> filters) {
        this.filters = filters;
    }
}
