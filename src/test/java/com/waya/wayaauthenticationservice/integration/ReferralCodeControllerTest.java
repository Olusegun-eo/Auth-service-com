package com.waya.wayaauthenticationservice.integration;

import com.waya.wayaauthenticationservice.entity.Profile;
import com.waya.wayaauthenticationservice.entity.ReferralCode;
import com.waya.wayaauthenticationservice.entity.Users;
import com.waya.wayaauthenticationservice.repository.ProfileRepository;
import com.waya.wayaauthenticationservice.repository.ReferralCodeRepository;
import com.waya.wayaauthenticationservice.repository.RolesRepository;
import com.waya.wayaauthenticationservice.repository.UserRepository;
import com.waya.wayaauthenticationservice.util.TestHelper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
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

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static com.waya.wayaauthenticationservice.util.SecurityConstants.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
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

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RolesRepository rolesRepository;

    private Users user = new Users();

    private TestHelper testHelper;
    ReferralCode referralCode;

    @BeforeAll
    void setUp() {
       List<ReferralCode> list = referralCodeRepository.findAll();
        System.out.println("MEET ME FOR HERE :::::::"+ list);


        testHelper = new TestHelper(userRepository, rolesRepository);
        user = testHelper.createTestUser();

        Profile profile = new Profile();
        profile.setEmail("de@app.com");
        profile.setPhoneNumber("0983");
        profile.setSurname("surname");
        profile.setFirstName("firstname");
        profile.setDeleted(false);
        profile.setUserId("124");
        Optional<Profile> profile1 = null;
        if (!profileRepository.existsByEmail(profile.getEmail())){
            profile = profileRepository.save(profile);
        }else{
            profile1 = profileRepository.findByUserId(false,profile.getUserId());
            profile = profile1.get();
        }

        referralCode = new ReferralCode();
        referralCode.setReferralCode("pqwe");
        referralCode.setUserId("124");
        referralCode.setProfile(profile);
        Optional<ReferralCode> referralCode1 = referralCodeRepository.findByUserId(referralCode.getUserId());
        if (!referralCode1.isPresent())
            referralCode = referralCodeRepository.save(referralCode);
        System.out.println("This is the saved {} ::" + referralCode);
    }

    @Test
    @DisplayName("get referral code successfully")
    void getUserReferralCode() throws Exception {
        System.out.println("getUserReferralCode {} ::" + referralCode.getUserId());
        getAndVerifyUserReferralCode(referralCode.getUserId(), status().isOk());
    }

    @Test
    @DisplayName("get referral code with invalid user id")
    void getUserReferralCodeWithInvalidUserId() throws Exception {
        System.out.println("getUserReferralCodeWithInvalidUserId {} ::" + referralCode.getUserId());
        getAndVerifyUserReferralCode("568", status().isNotFound());
    }

    private void getAndVerifyUserReferralCode(
            String userId, ResultMatcher expectedStatus
    ) throws Exception {
        mockMvc.perform(get("/api/v1/referral/referral-code/" + userId)
                .header("Authorization", generateToken(user))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(expectedStatus);
    }

    public String generateToken(Users user) {
        try {
            System.out.println("::::::GENERATE TOKEN:::::");
            String token = Jwts.builder().setSubject(user.getEmail())
                    .setExpiration(new Date(System.currentTimeMillis() + getExpiration() * 1000))
                    .signWith(SignatureAlgorithm.HS512, getSecret()).compact();
            System.out.println(":::::Token:::::");
            return TOKEN_PREFIX + token;
        } catch (Exception e) {
            throw new RuntimeException(e.fillInStackTrace());
        }
    }
}

