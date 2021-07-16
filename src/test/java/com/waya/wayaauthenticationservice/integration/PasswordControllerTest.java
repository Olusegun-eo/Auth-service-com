package com.waya.wayaauthenticationservice.integration;

import com.waya.wayaauthenticationservice.entity.Profile;
import com.waya.wayaauthenticationservice.entity.Users;
import com.waya.wayaauthenticationservice.pojo.userDTO.BaseUserPojo;
import com.waya.wayaauthenticationservice.repository.ProfileRepository;
import com.waya.wayaauthenticationservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PasswordControllerTest {

    Users user = new Users();
    Profile profile = new Profile();

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    BCryptPasswordEncoder passwordEncoder;

    @BeforeAll
    public void setUp() {
        user.setEmail("emmox55@gmail.com");
        user.setFirstName("Stan");
        user.setSurname("Toju");
        user.setActive(true);
        user.setPhoneNumber("2348104728022");
        user.setPassword(passwordEncoder.encode("test@123"));
        user.setName(String.format("%s %s", user.getFirstName(), user.getSurname()));
        user.setId(1l);
        if(userRepository.existsByEmail(user.getEmail())
                || userRepository.existsByPhoneNumber(user.getPhoneNumber()))
            user = userRepository.save(user);

        profile.setEmail(user.getEmail());
        profile.setFirstName(user.getFirstName());
        profile.setSurname(user.getSurname());
        profile.setUserId(String.valueOf(user.getId()));
        profile.setPhoneNumber(user.getPhoneNumber());

        if(profileRepository.existsByEmail(user.getEmail()))
            profileRepository.save(profile);
    }

    @Test
    @DisplayName("reset Password successfully")
    public void resetPasswordSuccessfully() throws Exception {

        resetPassword(user, "$.message", "Successful", status().isCreated());
    }

    private void resetPassword(Users user, String s, String successful, ResultMatcher created) {
    }


}
