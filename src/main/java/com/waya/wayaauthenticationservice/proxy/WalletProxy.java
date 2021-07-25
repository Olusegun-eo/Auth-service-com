package com.waya.wayaauthenticationservice.proxy;

import com.waya.wayaauthenticationservice.config.AuthClientConfiguration;
import com.waya.wayaauthenticationservice.pojo.others.CreateAccountPojo;
import com.waya.wayaauthenticationservice.pojo.others.CreateAccountResponse;
import com.waya.wayaauthenticationservice.pojo.others.WalletAccount;
import com.waya.wayaauthenticationservice.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@FeignClient(name = "TEMPORAL-WALLET-SERVICE", url = "${app.config.wallet-service.base-url}", configuration = AuthClientConfiguration.class)
public interface WalletProxy {
	
	@PostMapping("/create/cooperate/user")
	ResponseEntity<ApiResponse<CreateAccountResponse>> createCorporateAccount(@RequestBody CreateAccountPojo createAccountPojo);

	@PostMapping("/user/account")
	ResponseEntity<ApiResponse<CreateAccountResponse>> createUserAccount(@RequestBody CreateAccountPojo createAccountPojo);

	@GetMapping("/accounts/{userId}")
	ResponseEntity<ApiResponse<List<WalletAccount>>> fetchUsersWallets(@PathVariable("userId") Long userId, @RequestHeader("Authorization") String token);

	@DeleteMapping("/account/deleteAccount/{userId}")
	ResponseEntity<ApiResponse<String>> deleteAccountAccountByUserId(@PathVariable("userId") Long userId, @RequestHeader("Authorization") String token);
}
