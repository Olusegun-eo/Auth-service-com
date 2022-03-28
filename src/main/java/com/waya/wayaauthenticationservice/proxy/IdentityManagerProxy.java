package com.waya.wayaauthenticationservice.proxy;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.waya.wayaauthenticationservice.proxy.impl.ApiClientExceptionHandler;
import com.waya.wayaauthenticationservice.response.IdentityResponse;
import com.waya.wayaauthenticationservice.util.HandleFeignError;

@FeignClient(name = "IDTM-SERVICE-API", url = "${app.config.identity-service.base-url}")
public interface IdentityManagerProxy {
	
	@RequestMapping(method = RequestMethod.POST, value = "/waya-merchant/init/{userId}")
	@HandleFeignError(ApiClientExceptionHandler.class)
	IdentityResponse PostCreateMerchant(@PathVariable("userId") long userId);

}
