package com.lighting.routing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableEurekaClient
public class LightingRoutingApplication {
    public static void main(String[] args) {
        SpringApplication.run(LightingRoutingApplication.class, args);
    }
}
