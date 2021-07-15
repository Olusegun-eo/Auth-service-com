package com.waya.wayaauthenticationservice.integration;

import com.waya.wayaauthenticationservice.entity.OtherDetails;
import com.waya.wayaauthenticationservice.entity.Profile;
import com.waya.wayaauthenticationservice.repository.ProfileRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("application-test")
@SpringBootTest(properties = {"eureka.client.enabled=false"})
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SearchControllerTest {

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private MockMvc mockMvc;

    @BeforeAll
    void setUp() {
        seedData();
    }

    @Order(1)
    @Test
    void searchByName() throws Exception {
        searchAndVerifyProfileByName("appp", status().isOk());
    }

    @Order(2)
    @Test
    void searchByPhoneNumber() throws Exception {
        searchAndVerifyProfileByPhoneNumber("09123", status().isOk());
    }

    @Order(3)
    @Test
    void searchByEmail() throws Exception {
        searchAndVerifyProfileByEmail("cpdaa@app.com", status().isOk());
    }

    @Order(4)
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
//        mockMvc.perform(MockMvcRequestBuilders.get("/search-profile-name/"+name)
//                .header("Authorization","serial eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJlbW1veDU1QGdtYWlsLmNvbSIsImV4cCI6MTY1NzY1NjI0Nn0.xOCakRQLFNXqbSOI3b3jsFek5ybfOmdMCfZ71N1TQ2o")
//                .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(expectedStatus);
    }

    private void searchAndVerifyProfileByPhoneNumber(
            String phoneNumber, ResultMatcher expectedStatus
    ) throws Exception {
        mockMvc.perform(get("/search-profile-phoneNumber/"+phoneNumber)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(expectedStatus);
//        mockMvc.perform(MockMvcRequestBuilders.get("/search-profile-phoneNumber/"+phoneNumber)
//                .header("Authorization","serial eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJlbW1veDU1QGdtYWlsLmNvbSIsImV4cCI6MTY1NzY1NjI0Nn0.xOCakRQLFNXqbSOI3b3jsFek5ybfOmdMCfZ71N1TQ2o")
//                .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(expectedStatus);
    }

    private void searchAndVerifyProfileByEmail(
            String email, ResultMatcher expectedStatus
    ) throws Exception {
        mockMvc.perform(get("/search-profile-email/"+email)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(expectedStatus);
//        mockMvc.perform(MockMvcRequestBuilders.get("/search-profile-email/"+email)
//                .header("Authorization","serial eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJlbW1veDU1QGdtYWlsLmNvbSIsImV4cCI6MTY1NzY1NjI0Nn0.xOCakRQLFNXqbSOI3b3jsFek5ybfOmdMCfZ71N1TQ2o")
//                .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(expectedStatus);
    }

    private void searchAndVerifyProfileOrganisationName(
            String organisationName, ResultMatcher expectedStatus
    ) throws Exception {
        mockMvc.perform(get("/search-profile-organisationName/"+organisationName)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(expectedStatus);
//        mockMvc.perform(MockMvcRequestBuilders.get("/search-profile-organisationName/"+organisationName)
//                .header("Authorization","serial eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJlbW1veDU1QGdtYWlsLmNvbSIsImV4cCI6MTY1NzY1NjI0Nn0.xOCakRQLFNXqbSOI3b3jsFek5ybfOmdMCfZ71N1TQ2o")
//                .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(expectedStatus);
    }

    private void seedData() {

        //personal profile 1
        Profile profile = new Profile();
        profile.setGender("male");
        profile.setPhoneNumber("091233");
        profile.setEmail("codedaa@app.com");
        profile.setFirstName("appp");
        profile.setSurname("appp");
        profile.setState("state");
        profile.setCorporate(false);
        profile.setUserId("145231");
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
        corporate.setUserId("42114");
        corporate.setCorporate(true);
        corporate.setDeleted(false);
        corporate.setSurname("surname");
        corporate.setFirstName("first name");
        corporate.setEmail("cpdaa@app.com");
        corporate.setPhoneNumber("09123");
        corporate.setOtherDetails(otherDetails);

        profileRepository.save(corporate);

    }

    public String getToken(){
        return "serial eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJlbW1veDU1QGdtYWlsLmNvbSIsImV4cCI6MTY1NzY1NjI0Nn0.xOCakRQLFNXqbSOI3b3jsFek5ybfOmdMCfZ71N1TQ2o";
    }

}

