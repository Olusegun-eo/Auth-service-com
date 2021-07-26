package com.waya.wayaauthenticationservice.proxy;

import com.waya.wayaauthenticationservice.config.AuthClientConfiguration;
import com.waya.wayaauthenticationservice.pojo.others.CreateAccountPojo;
import com.waya.wayaauthenticationservice.pojo.others.CreateAccountResponse;
import com.waya.wayaauthenticationservice.pojo.others.WalletAccessPojo;
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
	ApiResponse<List<WalletAccount>> fetchUsersWallets(@PathVariable("userId") Long userId, @RequestHeader("Authorization") String token);

	@PostMapping("/user/account/access")
	ResponseEntity<ApiResponse<WalletAccount>> modifyUserWallet(@RequestBody WalletAccessPojo pojo, @RequestHeader("Authorization") String token);
}
