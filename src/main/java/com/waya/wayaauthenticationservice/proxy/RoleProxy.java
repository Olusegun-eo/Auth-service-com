package com.waya.wayaauthenticationservice.proxy;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.waya.wayaauthenticationservice.pojo.access.RoleAccessResponse;
import com.waya.wayaauthenticationservice.pojo.access.UserAccessDTO;
import com.waya.wayaauthenticationservice.proxy.impl.ApiClientExceptionHandler;
import com.waya.wayaauthenticationservice.util.HandleFeignError;

@FeignClient(name = "ROLE-SERVICE-API", url = "${app.config.access-service.base-url}")
public interface RoleProxy {
	
	@GetMapping("/api/v1/user-access/auth/{userId}/access/{key}")
    @HandleFeignError(ApiClientExceptionHandler.class)
	RoleAccessResponse GetUserAccess(@PathVariable("userId") Long userId, @PathVariable("key") String key);
	
	@PostMapping("/api/v1/user-access/auth/assign/{key}")
    @HandleFeignError(ApiClientExceptionHandler.class)
	RoleAccessResponse PostUserAccess(@RequestBody UserAccessDTO role, @PathVariable("key") String key);

}
