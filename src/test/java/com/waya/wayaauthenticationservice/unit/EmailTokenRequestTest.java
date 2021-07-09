package com.waya.wayaauthenticationservice.unit;

import com.waya.wayaauthenticationservice.pojo.EmailTokenRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class EmailTokenRequestTest {
    @Test
    void emailTokenRequest() {
        final EmailTokenRequest emailTokenRequest = new EmailTokenRequest();
        emailTokenRequest.setEmail("ss@app.com");
        emailTokenRequest.setFullName("fullName");

        Assertions.assertEquals("ss@app.com", emailTokenRequest.getEmail());
        Assertions.assertEquals("fullName", emailTokenRequest.getFullName());
    }
}
