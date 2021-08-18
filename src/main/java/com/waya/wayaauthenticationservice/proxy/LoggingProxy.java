package com.waya.wayaauthenticationservice.proxy;

import com.waya.wayaauthenticationservice.pojo.log.LogRequest;
import com.waya.wayaauthenticationservice.proxy.impl.ApiClientExceptionHandler;
import com.waya.wayaauthenticationservice.response.ApiResponseBody;
import com.waya.wayaauthenticationservice.util.HandleFeignError;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "LOGGING-SERVICE-API", url = "${app.config.logging-service.base-url}")
public interface LoggingProxy {

    @PostMapping("/api/v1/log/create")
    @HandleFeignError(ApiClientExceptionHandler.class)
    ApiResponseBody<?> saveNewLog(@RequestBody LogRequest logPojo);

}
