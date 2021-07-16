package com.waya.wayaauthenticationservice.unit;

import com.waya.wayaauthenticationservice.response.ReferralCodeResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ReferralCodeResponseTest {
    @Test
    void referralCode() {
        ReferralCodeResponse response = new ReferralCodeResponse("uw");
        response.setReferralCode("wre");

        Assertions.assertEquals("wre", response.getReferralCode());
    }
}
