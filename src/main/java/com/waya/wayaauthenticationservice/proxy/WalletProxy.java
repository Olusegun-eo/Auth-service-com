package com.waya.wayaauthenticationservice.proxy;

import com.waya.wayaauthenticationservice.config.AuthClientConfiguration;
import com.waya.wayaauthenticationservice.pojo.others.CreateAccountPojo;
import com.waya.wayaauthenticationservice.pojo.others.CreateAccountResponse;
import com.waya.wayaauthenticationservice.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@FeignClient(name = "TEMPORAL-WALLET-SERVICE", url = "${app.config.wallet-service.base-url}", configuration = AuthClientConfiguration.class)
public interface WalletProxy {

//	@GetMapping("/find/by/userId/{userId}")
//	List<MainWalletResponse> getWalletById(@PathVariable("userId") Long userId, @RequestHeader("Authorization") String token);
//
//	@GetMapping("/get/default/wallet")
//	ApiResponse<MainWalletResponse> getDefaultWallet(@RequestHeader("Authorization") String token);
	//wallet/user/account
	
	@PostMapping("/create/cooperate/user")
	ResponseEntity<ApiResponse<CreateAccountResponse>> createCorporateAccount(@RequestBody CreateAccountPojo createAccountPojo);

	@PostMapping("/user/account")
	ResponseEntity<ApiResponse<CreateAccountResponse>> createUserAccount(@RequestBody CreateAccountPojo createAccountPojo);

}
