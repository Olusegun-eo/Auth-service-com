package com.waya.wayaauthenticationservice.proxy;

import java.util.List;

import com.waya.wayaauthenticationservice.pojo.others.*;
import com.waya.wayaauthenticationservice.response.NewWalletResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.waya.wayaauthenticationservice.config.AuthClientConfiguration;
import com.waya.wayaauthenticationservice.proxy.impl.ApiClientExceptionHandler;
import com.waya.wayaauthenticationservice.response.ApiResponseBody;
import com.waya.wayaauthenticationservice.util.HandleFeignError;


@FeignClient(name = "TEMPORAL-WALLET-SERVICE", url = "${app.config.wallet-service.base-url}", configuration = AuthClientConfiguration.class)
public interface WalletProxy {
	
	@RequestMapping(method = RequestMethod.POST, value = "/create/cooperate/user")
	ApiResponseBody<CreateAccountResponse> createCorporateAccount(@RequestBody CreateAccountPojo createAccountPojo);

	@RequestMapping(method = RequestMethod.POST, value = "/user/account")
	ApiResponseBody<CreateAccountResponse> createUserAccount(@RequestBody CreateAccountPojo createAccountPojo);

	@GetMapping("/accounts/{userId}")
	ApiResponseBody<List<WalletAccount>> fetchUsersWallets(@PathVariable("userId") Long userId, @RequestHeader("Authorization") String token);

	@RequestMapping(method = RequestMethod.POST, value = "/user/account/access")
	@HandleFeignError(ApiClientExceptionHandler.class)
	ApiResponseBody<WalletAccount> modifyUserWallet(@RequestBody WalletAccessPojo pojo, @RequestHeader("Authorization") String token);

	@PostMapping("/official/user/transfer")   ///api/v1/wallet/official/user/transfer admin/commission/payment
	ResponseEntity<ApiResponseBody<List<WalletTransactionPojo>>> sendMoneyToWallet(BonusTransferRequest transfer, @RequestHeader("Authorization") String token);

	@PostMapping("/official/user/transfer")  //  event/charge/payment
	ResponseEntity<ApiResponseBody<List<WalletTransactionPojo>>> sendSignUpBonusToWallet(BonusTransferRequest transfer, @RequestHeader("Authorization") String token);

	@GetMapping("/default/{userId}") //  ===> returns single
	ResponseEntity<ApiResponseBody<NewWalletResponse>> getDefaultWallet(@PathVariable("userId") String userId, @RequestHeader("Authorization") String token);

	///api/v1/wallet/waya/official/account
	@GetMapping("/waya/official/account") //
	ResponseEntity<ApiResponseBody<List<NewWalletResponse>>> getWayaOfficialWallet(@RequestHeader("Authorization") String token);


}
