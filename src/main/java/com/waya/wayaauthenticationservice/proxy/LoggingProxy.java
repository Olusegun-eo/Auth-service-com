package com.waya.wayaauthenticationservice.proxy;

import com.waya.wayaauthenticationservice.pojo.log.LogRequest;
import com.waya.wayaauthenticationservice.response.ApiResponseBody;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "LOGGING-SERVICE-API")
public interface LoggingProxy {

    @PostMapping("/api/v1/log/create")
    ApiResponseBody<LogRequest> saveNewLog(@RequestBody LogRequest logPojo);

}
