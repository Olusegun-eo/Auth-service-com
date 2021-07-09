package com.waya.wayaauthenticationservice.unit;

import com.waya.wayaauthenticationservice.streams.RecipientsSMS;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class RecipientsSMSTest {
    @Test
    void recipientSms() {
        final RecipientsSMS recipientsSMS = new RecipientsSMS("joe", "092");
        recipientsSMS.setName("doe");
        recipientsSMS.setTelephone("034");

        Assertions.assertEquals("doe", recipientsSMS.getName());
        Assertions.assertEquals("034", recipientsSMS.getTelephone());
    }
}
