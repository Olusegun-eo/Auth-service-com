package com.waya.wayaauthenticationservice.proxy;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import com.waya.wayaauthenticationservice.config.AuthClientConfiguration;
import com.waya.wayaauthenticationservice.pojo.CreateAccountPojo;
import com.waya.wayaauthenticationservice.pojo.CreateAccountResponse;
import com.waya.wayaauthenticationservice.pojo.MainWalletResponse;
import com.waya.wayaauthenticationservice.pojo.WalletAccount;
import com.waya.wayaauthenticationservice.util.ApiResponse;


@FeignClient(name = "TEMPORAL-WALLET-SERVICE", url = "http://157.230.223.54:9009/wallet", configuration = AuthClientConfiguration.class)
public interface WalletProxy {

	@GetMapping("/find/by/userId/{userId}")
	List<MainWalletResponse> getWalletById(@PathVariable("userId") Long userId, @RequestHeader("Authorization") String token);
	
	@GetMapping("/get/default/wallet")
    ApiResponse<MainWalletResponse> getDefaultWallet(@RequestHeader("Authorization") String token);
	
	@PostMapping("/create/cooperate/user")
	ApiResponse<CreateAccountResponse> createCorporateAccount(@RequestBody CreateAccountPojo createAccountPojo);

	@GetMapping("/accounts/{user_id}")
	ApiResponse<List<WalletAccount>> getUsersWallet(@PathVariable("user_id") Long id);

}
