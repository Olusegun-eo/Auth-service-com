package com.waya.wayaauthenticationservice.unit;

import com.waya.wayaauthenticationservice.response.ProfileImageResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ProfileImageResponseTest {
    @Test
    void profileImageResponse() {
        ProfileImageResponse profileImageResponse = new ProfileImageResponse("image");
        profileImageResponse.setImageUrl("image2");

        Assertions.assertEquals("image2", profileImageResponse.getImageUrl());
    }
}
