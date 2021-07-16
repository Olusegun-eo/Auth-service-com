package com.waya.wayaauthenticationservice.unit;

import com.waya.wayaauthenticationservice.streams.StreamDataEmail;
import com.waya.wayaauthenticationservice.streams.StreamPayload;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;

public class StreamPayloadTest {

    @Test
    void streamPayload() {

        final StreamDataEmail streamDataEmail = new StreamDataEmail();
        streamDataEmail.setMessage("message");
        streamDataEmail.setNames(Collections.emptyList());

        final StreamPayload<StreamDataEmail> streamPayload = new StreamPayload<>();
        streamPayload.setKey("key");
        streamPayload.setInitiator("initiator");
        streamPayload.setEventType("eventType");
        streamPayload.setToken("token");
        streamPayload.setData(streamDataEmail);

        Assertions.assertEquals("eventType", streamPayload.getEventType());
        Assertions.assertEquals("initiator", streamPayload.getInitiator());
        Assertions.assertEquals("token", streamPayload.getToken());
        Assertions.assertEquals("key", streamPayload.getKey());
        Assertions.assertEquals("message", streamPayload.getData().getMessage());
        Assertions.assertEquals(0, streamPayload.getData().getNames().size());
    }
}
