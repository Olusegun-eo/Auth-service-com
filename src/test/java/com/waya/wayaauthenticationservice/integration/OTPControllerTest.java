package com.waya.wayaauthenticationservice.integration;

import com.waya.wayaauthenticationservice.entity.OTPBase;
import com.waya.wayaauthenticationservice.repository.OTPRepository;
import org.hamcrest.core.Is;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@ActiveProfiles("test")
@SpringBootTest(properties = {"eureka.client.enabled=false"})
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OTPControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OTPRepository otpRepository;

    OTPBase otpBaseValid = new OTPBase();
    OTPBase otpBaseInValid = new OTPBase();

    @BeforeAll
    public void setUp() {
        setOtpData();
    }

    @Test
    void verifyOtp() throws Exception {
        getAndVerifyOtp(otpBaseValid.getPhoneNumber(), otpBaseValid.getCode(),
                "$.message", "OTP verified successfully",
                "$.status", true);
    }

    @Test
    void verifyOtpWhenOtpIsNotValid() throws Exception {
        getAndVerifyOtp(otpBaseInValid.getPhoneNumber(), otpBaseInValid.getCode(),
                "$.message", "OTP has expired",
                "$.status", false);
    }

    @Test
    void verifyOtpWithInvalidDetails() throws Exception {
        getAndVerifyOtp("19230495", otpBaseInValid.getCode(),
                "$.message", "Invalid OTP",
                "$.status", false);
    }


    private void getAndVerifyOtp(final String phone, Integer otp,
                                 String jsonPath0, String jsonPathMessage0,
                                 String jsonPath1, boolean jsonPathMessage1) throws Exception {
        mockMvc.perform(get("/otp-verify/{phoneNumber}/{otp}",
                phone, otp, otpBaseValid.getCode())
                .contentType(APPLICATION_JSON))
                .andExpect(jsonPath(jsonPath0, Is.is(jsonPathMessage0)))
                .andExpect(jsonPath(jsonPath1, Is.is(jsonPathMessage1)));

    }

    private void setOtpData() {
        otpBaseValid.setEmail("app@app.com");
        otpBaseValid.setCode(123456);
        otpBaseValid.setValid(true);
        otpBaseValid.setPhoneNumber("01928374");
        otpBaseValid.setExpiryDate(10);
        otpRepository.save(otpBaseValid);

        otpBaseInValid.setEmail("app3@app.com");
        otpBaseInValid.setCode(125456);
        otpBaseInValid.setValid(false);
        otpBaseInValid.setPhoneNumber("01928374");
        otpBaseInValid.setExpiryDate(0);
        otpRepository.save(otpBaseInValid);
    }
}
