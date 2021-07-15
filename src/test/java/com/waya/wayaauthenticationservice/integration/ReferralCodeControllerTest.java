package com.waya.wayaauthenticationservice.integration;

import com.waya.wayaauthenticationservice.entity.Profile;
import com.waya.wayaauthenticationservice.entity.ReferralCode;
import com.waya.wayaauthenticationservice.repository.ProfileRepository;
import com.waya.wayaauthenticationservice.repository.ReferralCodeRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;

import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("application-test")
@SpringBootTest(properties = {"eureka.client.enabled=false"})
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ReferralCodeControllerTest {

    @Autowired
    private ReferralCodeRepository referralCodeRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private MockMvc mockMvc;

    @BeforeAll
    void setUp() {

        Profile profile = new Profile();
        profile.setEmail("de@app.com");
        profile.setPhoneNumber("0983");
        profile.setSurname("surname");
        profile.setFirstName("firstname");
        profile.setDeleted(false);
        profile.setUserId("124");
        profileRepository.save(profile);

        ReferralCode referralCode = new ReferralCode();
        referralCode.setReferalCode("pqwe");
        referralCode.setUserId("124");
        referralCode.setProfile(profile);
        Optional<ReferralCode> referralCode1 = referralCodeRepository.findByUserId(referralCode.getUserId());
        if (!referralCode1.isPresent());
            referralCodeRepository.save(referralCode);
    }

    @Test
    @DisplayName("get referral code successfully")
    void getUserReferralCode() throws Exception {
        getAndVerifyUserReferralCode("124", status().isOk());
    }

    @Test
    @DisplayName("get referral code with invalid user id")
    void getUserReferralCodeWithInvalidUserId() throws Exception {
        getAndVerifyUserReferralCode("12431", status().isNotFound());
    }

    private void getAndVerifyUserReferralCode(
            String userId, ResultMatcher expectedStatus
    ) throws Exception {
        mockMvc.perform(get("/referral-code/" + userId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(expectedStatus);
    }
}

