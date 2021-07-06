package com.waya.wayaauthenticationservice.proxy;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "referral-code-client", url = "${app.config.referral.base-url}")
public class ReferralProxy {

    // make a call to save referral code

    //

}