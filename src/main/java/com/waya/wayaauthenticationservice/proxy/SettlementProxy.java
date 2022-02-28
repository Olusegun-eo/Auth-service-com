package com.waya.wayaauthenticationservice.proxy;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.waya.wayaauthenticationservice.pojo.SettleUserRequest;
import com.waya.wayaauthenticationservice.proxy.impl.ApiClientExceptionHandler;
import com.waya.wayaauthenticationservice.response.SettleUserResponse;
import com.waya.wayaauthenticationservice.util.HandleFeignError;

@FeignClient(name = "SETTLEMENT-SERVICE-API", url = "${app.config.settle-service.base-url}")
public interface SettlementProxy {
	
	@RequestMapping(method = RequestMethod.GET, value = "/user/auth/{userId}/{keyValue}")
	@HandleFeignError(ApiClientExceptionHandler.class)
	SettleUserResponse GetSettleUser(@PathVariable("userId") long userId, @PathVariable("keyValue") String keyValue);
	
	@RequestMapping(method = RequestMethod.POST, value = "/user/create/auth/{keyValue}")
	@HandleFeignError(ApiClientExceptionHandler.class)
	SettleUserResponse PostSettleUser(@RequestBody SettleUserRequest user, @PathVariable("keyValue") String keyValue);

}
