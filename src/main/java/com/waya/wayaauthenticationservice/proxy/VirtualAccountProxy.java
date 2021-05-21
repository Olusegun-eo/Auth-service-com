package com.waya.wayaauthenticationservice.proxy;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import com.waya.wayaauthenticationservice.config.AuthClientConfiguration;
import com.waya.wayaauthenticationservice.pojo.VirtualAccountPojo;

@FeignClient(name = "ACCOUNT-CREATION-SERVICE", url = "http://46.101.41.187:7090/account-creation-service/api", configuration = AuthClientConfiguration.class)
public interface VirtualAccountProxy {
	
	@PostMapping("/account/createVirtualAccount")
	ResponseEntity<String> createVirtualAccount(VirtualAccountPojo virtualAccountPojo, @RequestHeader("Authorization") String token);
}
