package com.waya.wayaauthenticationservice.unit;

import com.waya.wayaauthenticationservice.entity.Profile;
import com.waya.wayaauthenticationservice.entity.ReferralCode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

public class ReferralCodeTest {
    @Test
    void referralCode() {

        LocalDateTime dateTime = LocalDateTime.now();

        Profile profile = new Profile();
        profile.setEmail("e@app.com");
        profile.setSurname("surname");

        final ReferralCode referralCode = new ReferralCode();
        referralCode.setReferralCode("092kd");
        referralCode.setCreatedAt(dateTime);
        referralCode.setProfile(profile);
        referralCode.setUserId("iwr");
        referralCode.setId(UUID.fromString("66cc93ed-2bd6-4355-a7b7-06299b8d2746"));

        Assertions.assertEquals("092kd", referralCode.getReferralCode());
        Assertions.assertEquals("iwr", referralCode.getUserId());
        Assertions.assertEquals(dateTime, referralCode.getCreatedAt());
        Assertions.assertEquals("surname", referralCode.getProfile().getSurname());
        Assertions.assertEquals("e@app.com", referralCode.getProfile().getEmail());
    }

}
