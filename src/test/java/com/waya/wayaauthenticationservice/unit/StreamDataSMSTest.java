package com.waya.wayaauthenticationservice.unit;

import com.waya.wayaauthenticationservice.streams.StreamDataSMS;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;

public class StreamDataSMSTest {
    @Test
    void streamDataSms() {
        final StreamDataSMS streamDataSMS = new StreamDataSMS();
        streamDataSMS.setMessage("message");
        streamDataSMS.setRecipients(Collections.emptyList());

        Assertions.assertEquals("message", streamDataSMS.getMessage());
        Assertions.assertEquals(0, streamDataSMS.getRecipients().size());
    }
}
