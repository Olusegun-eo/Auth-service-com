package com.waya.wayaauthenticationservice.proxy;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.waya.wayaauthenticationservice.config.AuthClientConfiguration;
import com.waya.wayaauthenticationservice.pojo.others.CreateAccountPojo;
import com.waya.wayaauthenticationservice.pojo.others.CreateAccountResponse;
import com.waya.wayaauthenticationservice.pojo.others.WalletAccessPojo;
import com.waya.wayaauthenticationservice.pojo.others.WalletAccount;
import com.waya.wayaauthenticationservice.proxy.impl.ApiClientExceptionHandler;
import com.waya.wayaauthenticationservice.response.ApiResponse;
import com.waya.wayaauthenticationservice.util.HandleFeignError;


@FeignClient(name = "TEMPORAL-WALLET-SERVICE", url = "${app.config.wallet-service.base-url}", configuration = AuthClientConfiguration.class)
public interface WalletProxy {
	
	@RequestMapping(method = RequestMethod.POST, value = "/create/cooperate/user")
	ApiResponse<CreateAccountResponse> createCorporateAccount(@RequestBody CreateAccountPojo createAccountPojo);

	@RequestMapping(method = RequestMethod.POST, value = "/user/account")
	ApiResponse<CreateAccountResponse> createUserAccount(@RequestBody CreateAccountPojo createAccountPojo);

	@GetMapping("/accounts/{userId}")
	ApiResponse<List<WalletAccount>> fetchUsersWallets(@PathVariable("userId") Long userId, @RequestHeader("Authorization") String token);

	@RequestMapping(method = RequestMethod.POST, value = "/user/account/access")
	@HandleFeignError(ApiClientExceptionHandler.class)
	ApiResponse<WalletAccount> modifyUserWallet(@RequestBody WalletAccessPojo pojo, @RequestHeader("Authorization") String token);
}
