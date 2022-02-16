package com.waya.wayaauthenticationservice.proxy;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.waya.wayaauthenticationservice.pojo.kyc.KycAuthUpdate;
import com.waya.wayaauthenticationservice.pojo.kyc.KycStatus;
import com.waya.wayaauthenticationservice.proxy.impl.ApiClientExceptionHandler;
import com.waya.wayaauthenticationservice.response.ApiResponseBody;
import com.waya.wayaauthenticationservice.util.HandleFeignError;

@FeignClient(name = "KYC-SERVICE-API")
public interface KycProxy {
	
	@RequestMapping(method = RequestMethod.GET, value = "/kyc/users/status/{key}")
	@HandleFeignError(ApiClientExceptionHandler.class)
	ApiResponseBody<List<KycStatus>> GetUserKyc(@PathVariable("key") String key);
	
	@RequestMapping(method = RequestMethod.GET, value = "/kyc/users/status/{key}/{userId}")
	@HandleFeignError(ApiClientExceptionHandler.class)
	ApiResponseBody<KycStatus> GetByUserKyc(@PathVariable("key") String key, @PathVariable("userId") Long userId);
	
	@RequestMapping(method = RequestMethod.PUT, value = "/kyc/users/update/{key}")
	@HandleFeignError(ApiClientExceptionHandler.class)
	ApiResponseBody<KycStatus> PostKycUpdate(@PathVariable("key") String key, 
    		@RequestBody KycAuthUpdate kyc);
	
	@RequestMapping(method = RequestMethod.POST, value = "/kyc/users/post/{key}")
	@HandleFeignError(ApiClientExceptionHandler.class)
	ApiResponseBody<KycStatus> PostKyc(@PathVariable("key") String key, 
    		@RequestBody KycAuthUpdate kyc);

}
