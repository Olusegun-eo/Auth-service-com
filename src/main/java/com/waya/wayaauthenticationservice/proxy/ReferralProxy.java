package com.waya.wayaauthenticationservice.proxy;

import com.waya.wayaauthenticationservice.pojo.others.ProfileDto;
import com.waya.wayaauthenticationservice.pojo.others.ReferralCodePojo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "referral-code-client", url = "${app.config.referral.base-url}")
public interface ReferralProxy {

    // make a call to save referral code
    @GetMapping("/find/by/userId/{userId}")
    ReferralCodePojo getReferralCodeByUserId(@PathVariable("userId") String userId);

    @GetMapping("/get-user-by-referral-code/{userId}")
    ReferralCodePojo getUserByReferralCode(@PathVariable("userId") String userId);

    @PostMapping("/save-referral-code")
    ResponseEntity<String> saveReferralCode(@RequestBody ProfileDto newProfile, @RequestParam("userId") String userId);

}