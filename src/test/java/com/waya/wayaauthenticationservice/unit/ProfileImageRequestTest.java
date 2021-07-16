package com.waya.wayaauthenticationservice.unit;

import com.waya.wayaauthenticationservice.pojo.others.ProfileImageRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ProfileImageRequestTest {
    @Test
    void profileImageRequest() {
        final ProfileImageRequest profileImageRequest = new ProfileImageRequest();
        profileImageRequest.setProfileImage("image");

        Assertions.assertEquals("image", profileImageRequest.getProfileImage());
    }
}
