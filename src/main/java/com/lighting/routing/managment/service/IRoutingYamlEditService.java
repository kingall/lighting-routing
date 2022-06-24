package com.lighting.routing.managment.service;

import com.lighting.routing.managment.entity.Routing;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

public interface IRoutingYamlEditService {
    public List<Object> getRoutingList();

    public void addRouting(Routing routing) throws IOException, URISyntaxException;
}
