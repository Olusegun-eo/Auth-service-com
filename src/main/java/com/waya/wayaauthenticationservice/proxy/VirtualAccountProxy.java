package com.waya.wayaauthenticationservice.proxy;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import com.waya.wayaauthenticationservice.config.AuthClientConfiguration;
import com.waya.wayaauthenticationservice.pojo.others.VirtualAccountPojo;
import com.waya.wayaauthenticationservice.pojo.others.VirtualAccountResponse;
import com.waya.wayaauthenticationservice.response.ApiResponseBody;

@FeignClient(name = "ACCOUNT-CREATION-SERVICE", url = "${app.config.virtual-account.base-url}", configuration = AuthClientConfiguration.class)
public interface VirtualAccountProxy {
	
	@PostMapping("/account/createVirtualAccount")
	ApiResponseBody<VirtualAccountResponse> createVirtualAccount(@RequestBody VirtualAccountPojo virtualAccountPojo);

	@DeleteMapping("/account/deleteAccount/{userId}")
	ApiResponseBody<VirtualAccountResponse> deleteAccountByUserId(@PathVariable("userId") Long userId, @RequestHeader("Authorization") String token);

	@GetMapping("/account/getAccounts/{userId}")
	ApiResponseBody<VirtualAccountResponse> fetchAccountByUserId(@PathVariable("userId") Long userId, @RequestHeader("Authorization") String token);
}
