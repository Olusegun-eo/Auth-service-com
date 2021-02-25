package com.waya.wayaauthenticationservice.controller;

import com.waya.wayaauthenticationservice.entity.Users;
import com.waya.wayaauthenticationservice.pojo.UserPojo;
import com.waya.wayaauthenticationservice.repository.UserRepository;
import org.hamcrest.core.Is;
import org.junit.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;

import static com.waya.wayaauthenticationservice.util.JsonString.asJsonString;
import static org.junit.Assert.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

//@ActiveProfiles("test")
//@SpringBootTest
//@AutoConfigureMockMvc
//@TestInstance(TestInstance.Lifecycle.PER_CLASS)
//public class AuthenticationControllerTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @Autowired
//    private UserRepository userRepository;
//
//    Users user = new Users();
//    Users userPojo = new UserPojo();
//
//    @BeforeAll
//    public void setUp() {
//        user.setEmail("stan@toju.com");
//        user.setFirstName("Stan");
//        user.setSurname("Toju");
//        user.setPhoneNumber("0029934");
//        user.setId(1);
//        user.setCorporate(false);
//        user.setEmailVerified(false);
//        userRepository.save(user);
//    }
//
//
//    @Test
//    @DisplayName("create personal profile successfully")
//    public void createUser() throws Exception {
//        UserPojo userPojo = new UserPojo();
//
//
//        createNewUser(user, "$.message", "profile created successfully", status().isCreated());
//    }
//
//
//    private void createNewUser(UserPojo userPojo, String jsonPath, String jsonPathMessage, ResultMatcher expectedStatus) throws Exception {
//
//        mockMvc.perform(post("/profile")
//                .contentType(APPLICATION_JSON).content(asJsonString(user)))
//                .andExpect(expectedStatus)
//                .andExpect(jsonPath(jsonPath, Is.is(jsonPathMessage)));
//    }

//}