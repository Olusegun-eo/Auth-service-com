package com.waya.wayaauthenticationservice.proxy;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.waya.wayaauthenticationservice.pojo.kyc.KycStatus;
import com.waya.wayaauthenticationservice.proxy.impl.ApiClientExceptionHandler;
import com.waya.wayaauthenticationservice.response.ApiResponseBody;
import com.waya.wayaauthenticationservice.util.HandleFeignError;

@FeignClient(name = "KYC-SERVICE-API", url = "${app.config.kyc-service.base-url}")
public interface KycProxy {
	
	@RequestMapping(method = RequestMethod.POST, value = "/users/kyc/staus")
	@HandleFeignError(ApiClientExceptionHandler.class)
	ApiResponseBody<List<KycStatus>> GetUserKyc();

}
