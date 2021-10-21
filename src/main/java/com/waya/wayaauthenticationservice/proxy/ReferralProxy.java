package com.waya.wayaauthenticationservice.proxy;

import com.waya.wayaauthenticationservice.pojo.others.ReferralCodePojo;
import com.waya.wayaauthenticationservice.response.ApiResponseBody;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "REFERRAL-SERVICE-API", url = "${app.config.referral-service.base-url}")
public interface ReferralProxy {

    @GetMapping("/get-user-by-referral-code/{userId}")
    ResponseEntity<ApiResponseBody<ReferralCodePojo>> getUserByReferralCode(@PathVariable String userId, @RequestHeader("Authorization") String token);
}
