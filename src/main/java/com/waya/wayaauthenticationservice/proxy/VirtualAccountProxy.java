package com.waya.wayaauthenticationservice.proxy;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import com.waya.wayaauthenticationservice.config.AuthClientConfiguration;
import com.waya.wayaauthenticationservice.pojo.others.VirtualAccountPojo;

@FeignClient(name = "ACCOUNT-CREATION-SERVICE", url = "${app.config.virtual-account.base-url}", configuration = AuthClientConfiguration.class)
public interface VirtualAccountProxy {
	
	@PostMapping("/account/createVirtualAccount")
	ResponseEntity<String> createVirtualAccount(VirtualAccountPojo virtualAccountPojo, @RequestHeader("Authorization") String token);
}
