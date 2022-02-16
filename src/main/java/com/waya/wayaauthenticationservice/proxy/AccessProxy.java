package com.waya.wayaauthenticationservice.proxy;

import com.waya.wayaauthenticationservice.pojo.access.UserAccessResponse;
import com.waya.wayaauthenticationservice.proxy.impl.ApiClientExceptionHandler;
import com.waya.wayaauthenticationservice.response.ApiResponseBody;
import com.waya.wayaauthenticationservice.util.HandleFeignError;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "ACCESS-SERVICE-API")
public interface AccessProxy {

    @GetMapping("/api/v1/user-role/auth/{userId}")
    @HandleFeignError(ApiClientExceptionHandler.class)
    ApiResponseBody<UserAccessResponse> GetUsersAccess(@PathVariable Long userId);

}
