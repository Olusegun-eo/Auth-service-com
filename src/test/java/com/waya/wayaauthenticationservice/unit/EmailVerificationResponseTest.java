package com.waya.wayaauthenticationservice.unit;

import com.waya.wayaauthenticationservice.response.EmailVerificationResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class EmailVerificationResponseTest {
    @Test
    void emailVerificationResponse() {

        EmailVerificationResponse emailVerificationResponse =
                new EmailVerificationResponse(true, "otp is valid");

        emailVerificationResponse.setMessage("invalid");
        emailVerificationResponse.setValid(false);

        Assertions.assertEquals("invalid", emailVerificationResponse.getMessage());
        Assertions.assertFalse(emailVerificationResponse.isValid());
    }
}
