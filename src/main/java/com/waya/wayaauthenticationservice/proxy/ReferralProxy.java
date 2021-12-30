package com.waya.wayaauthenticationservice.proxy;

import com.waya.wayaauthenticationservice.pojo.others.AutoSignUpReferralRequest;
import com.waya.wayaauthenticationservice.pojo.others.ReferralCodePojo;
import com.waya.wayaauthenticationservice.pojo.others.ReferralCodeRequest;
import com.waya.wayaauthenticationservice.response.ApiResponseBody;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "REFERRAL-SERVICE-API", url = "${app.config.referral-service.base-url}")
public interface ReferralProxy {

    @GetMapping("/api/v1/get-user-by-referral-code/{userId}")
    ResponseEntity<ApiResponseBody<ReferralCodePojo>> getUserByReferralCode(@PathVariable String userId);

    @PostMapping("/api/v1/referralcode")
    ResponseEntity<String> saveReferralCode(@RequestBody ReferralCodeRequest referralCodeRequest);

    @PostMapping("/api/v1/users/auto-send-referral-bonus")
    ResponseEntity<String> autoSendSignUpReferralBonus(@RequestBody AutoSignUpReferralRequest referralRequest, @RequestHeader("Authorization") String token );

    @GetMapping("/api/v1/referralcode/get-details/{referralCode}")
    ResponseEntity<ApiResponseBody<ReferralCodePojo>> getReferralCodeByCode(@PathVariable String userId, @RequestHeader("Authorization") String token);


}