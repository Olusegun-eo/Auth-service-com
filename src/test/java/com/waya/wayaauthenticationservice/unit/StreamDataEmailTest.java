package com.waya.wayaauthenticationservice.unit;

import com.waya.wayaauthenticationservice.streams.StreamDataEmail;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;

public class StreamDataEmailTest {
    @Test
    void streamDataEmail() {
        final StreamDataEmail streamDataEmail = new StreamDataEmail();
        streamDataEmail.setNames(Collections.emptyList());
        streamDataEmail.setMessage("message");

        Assertions.assertEquals("message", streamDataEmail.getMessage());
        Assertions.assertEquals(0, streamDataEmail.getNames().size());
    }
}
