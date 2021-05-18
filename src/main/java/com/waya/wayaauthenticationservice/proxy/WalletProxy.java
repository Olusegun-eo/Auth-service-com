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


@FeignClient(name = "TEMPORAL-WALLET-SERVICE", url = "http://157.230.223.54:9009", configuration = AuthClientConfiguration.class)
public interface WalletProxy {

	@GetMapping("/wallet/find/by/userId/{userId}")
	public List<MainWalletResponse> getWalletById(@PathVariable("userId") Long userId, @RequestHeader("Authorization") String token);
	
	@GetMapping("/wallet/get/default/wallet")
    MainWalletResponse getDefaultWallet(@RequestHeader("Authorization") String token);
	
	@PostMapping("/wallet/create/cooperate/user")
	CreateAccountResponse createCooperateAccouont(@RequestBody CreateAccountPojo createAccountPojo, @RequestHeader("Authorization") String token);

}
