package com.waya.wayaauthenticationservice.unit;

import com.waya.wayaauthenticationservice.streams.RecipientsEmail;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class RecipientsEmailTest {
    @Test
    void recipientEmail() {
        RecipientsEmail recipientsEmail = new RecipientsEmail("ss@app.com", "joe");
        recipientsEmail.setEmail("as@app.com");
        recipientsEmail.setFullName("doe");

        Assertions.assertEquals("as@app.com", recipientsEmail.getEmail());
        Assertions.assertEquals("doe", recipientsEmail.getFullName());
    }
}
