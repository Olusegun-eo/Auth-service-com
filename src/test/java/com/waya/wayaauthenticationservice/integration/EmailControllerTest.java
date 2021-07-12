//package com.waya.wayaauthenticationservice.integration;
//
//import com.waya.wayaauthenticationservice.entity.OTPBase;
//import com.waya.wayaauthenticationservice.repository.OTPRepository;
//import org.hamcrest.core.Is;
//import org.junit.jupiter.api.BeforeAll;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.TestInstance;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.test.web.servlet.ResultMatcher;
//
//import static org.springframework.http.MediaType.APPLICATION_JSON;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
//import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//@ActiveProfiles("test")
//@SpringBootTest(properties = {"eureka.client.enabled=false"})
//@AutoConfigureMockMvc
//@TestInstance(TestInstance.Lifecycle.PER_CLASS)
//class EmailControllerTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @Autowired
//    private OTPRepository otpRepository;
//
//    OTPBase otpBaseValid = new OTPBase();
//    OTPBase otpBaseInValid = new OTPBase();
//
//    @BeforeAll
//    public void setUp() {
//        setOtpData();
//    }
//
//    @Test
//    void sendEmailToken() throws Exception {
//        getEmailToken(otpBaseValid.getEmail(), "john", "$.message",
//                "A token has been sent to your email", status().isOk());
//    }
//
//    private void getEmailToken(final String email, final String userName,
//                               final String jsonPath, final String jsonPathMessage,
//                               ResultMatcher expectedStatus) throws Exception {
//        mockMvc.perform(get("/email-token/{email}/{userName}", email, userName)
//                .contentType(APPLICATION_JSON))
//                .andExpect(jsonPath(jsonPath, Is.is(jsonPathMessage)))
//                .andExpect(expectedStatus).andDo(print());
//    }
//
//    private void setOtpData() {
//        otpBaseValid.setEmail("app@app.com");
//        otpBaseValid.setCode(123456);
//        otpBaseValid.setValid(true);
//        otpBaseValid.setExpiryDate(10);
//        otpRepository.save(otpBaseValid);
//
//        otpBaseInValid.setEmail("app3@app.com");
//        otpBaseInValid.setCode(125456);
//        otpBaseInValid.setValid(false);
//        otpBaseInValid.setExpiryDate(0);
//        otpRepository.save(otpBaseInValid);
//    }
//}
