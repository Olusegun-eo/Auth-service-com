package com.waya.wayaauthenticationservice.proxy;

import com.waya.wayaauthenticationservice.config.AuthClientConfiguration;
import com.waya.wayaauthenticationservice.pojo.others.VirtualAccountPojo;
import com.waya.wayaauthenticationservice.pojo.others.VirtualAccountResponse;
import com.waya.wayaauthenticationservice.response.ApiResponseBody;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "ACCOUNT-CREATION-SERVICE", url = "${app.config.virtual-account.base-url}", configuration = AuthClientConfiguration.class)
public interface VirtualAccountProxy {
	
	@PostMapping("/account/createVirtualAccount")
	ResponseEntity<ApiResponseBody<VirtualAccountResponse>> createVirtualAccount(@RequestBody VirtualAccountPojo virtualAccountPojo);

	@DeleteMapping("/account/deleteAccount/{userId}")
	ResponseEntity<ApiResponseBody<VirtualAccountResponse>> deleteAccountByUserId(@PathVariable("userId") Long userId, @RequestHeader("Authorization") String token);

	@GetMapping("/account/getAccounts/{userId}")
	ResponseEntity<ApiResponseBody<VirtualAccountResponse>> fetchAccountByUserId(@PathVariable("userId") Long userId, @RequestHeader("Authorization") String token);
}
