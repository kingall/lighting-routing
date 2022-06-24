package com.lighting.routing.log;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;

/**
 * 路由日志记录
 */
@Slf4j
public class RoutingLogWriter {
    public static void writeRoutingLog(ApiLog apiLog) {
        log.info(JSONObject.toJSONString(apiLog));
    }
}
