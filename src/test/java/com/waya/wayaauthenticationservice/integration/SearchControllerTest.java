package com.waya.wayaauthenticationservice.integration;

import com.waya.wayaauthenticationservice.entity.OtherDetails;
import com.waya.wayaauthenticationservice.entity.Profile;
import com.waya.wayaauthenticationservice.repository.ProfileRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest(properties = {"eureka.client.enabled=false"})
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SearchControllerTest {

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private MockMvc mockMvc;

    @BeforeAll
    void setUp() {
        seedData();
    }

    @Test
    void searchByName() throws Exception {
        searchAndVerifyProfileByName("app", status().isOk());
    }

    @Test
    void searchByPhoneNumber() throws Exception {
        searchAndVerifyProfileByPhoneNumber("09123", status().isOk());
    }

    @Test
    void searchByEmail() throws Exception {
        searchAndVerifyProfileByEmail("cpda@app.com", status().isOk());
    }

    @Test
    void searchByOrganisationName() throws Exception {
        searchAndVerifyProfileOrganisationName("name", status().isOk());
    }

    private void searchAndVerifyProfileByName(
            String name, ResultMatcher expectedStatus
    ) throws Exception {
        mockMvc.perform(get("/search-profile-name/"+name)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(expectedStatus);
    }

    private void searchAndVerifyProfileByPhoneNumber(
            String phoneNumber, ResultMatcher expectedStatus
    ) throws Exception {
        mockMvc.perform(get("/search-profile-phoneNumber/"+phoneNumber)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(expectedStatus);
    }

    private void searchAndVerifyProfileByEmail(
            String email, ResultMatcher expectedStatus
    ) throws Exception {
        mockMvc.perform(get("/search-profile-email/"+email)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(expectedStatus);
    }

    private void searchAndVerifyProfileOrganisationName(
            String organisationName, ResultMatcher expectedStatus
    ) throws Exception {
        mockMvc.perform(get("/search-profile-organisationName/"+organisationName)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(expectedStatus);
    }

    private void seedData() {

        //personal profile 1
        Profile profile = new Profile();
        profile.setGender("male");
        profile.setPhoneNumber("09123");
        profile.setEmail("code@app.com");
        profile.setFirstName("app");
        profile.setSurname("app");
        profile.setState("state");
        profile.setCorporate(false);
        profile.setUserId("14523");
        profile.setDeleted(false);

        profileRepository.save(profile);

        //corporate profile 1
        OtherDetails otherDetails = new OtherDetails();
        otherDetails.setOrganisationType("organisationType");
        otherDetails.setBusinessType("businessType");
        otherDetails.setOrganisationName("organisation name");

        Profile corporate = new Profile();
        corporate.setOrganisationName("name");
        corporate.setCity("city");
        corporate.setReferral("");
        corporate.setUserId("4214");
        corporate.setCorporate(true);
        corporate.setDeleted(false);
        corporate.setSurname("surname");
        corporate.setFirstName("first name");
        corporate.setEmail("cpda@app.com");
        corporate.setPhoneNumber("09123");
        corporate.setOtherDetails(otherDetails);

        profileRepository.save(corporate);
    }

}

