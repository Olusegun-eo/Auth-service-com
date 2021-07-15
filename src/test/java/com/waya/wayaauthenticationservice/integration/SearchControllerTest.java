package com.waya.wayaauthenticationservice.integration;

import com.waya.wayaauthenticationservice.entity.OtherDetails;
import com.waya.wayaauthenticationservice.entity.Profile;
import com.waya.wayaauthenticationservice.entity.Users;
import com.waya.wayaauthenticationservice.repository.ProfileRepository;
import com.waya.wayaauthenticationservice.repository.UserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;

import java.time.LocalDateTime;
import java.util.Date;

import static com.waya.wayaauthenticationservice.util.Constant.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest(properties = {"eureka.client.enabled=false"})
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SearchControllerTest {

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    private final Users user = new Users();

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

        mockMvc.perform(get("/api/v1/search/search-profile-name/"+name)
                .header("Authorization","serial eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJlbW1veDU1QGdtYWlsLmNvbSIsImV4cCI6MTY1NzY1NjI0Nn0.xOCakRQLFNXqbSOI3b3jsFek5ybfOmdMCfZ71N1TQ2o")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(expectedStatus);
    }

    private void searchAndVerifyProfileByPhoneNumber(
            String phoneNumber, ResultMatcher expectedStatus
    ) throws Exception {
        mockMvc.perform(get("/api/v1/search/search-profile-phoneNumber/"+phoneNumber)
                .header("Authorization","serial eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJlbW1veDU1QGdtYWlsLmNvbSIsImV4cCI6MTY1NzY1NjI0Nn0.xOCakRQLFNXqbSOI3b3jsFek5ybfOmdMCfZ71N1TQ2o")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(expectedStatus);
    }

    private void searchAndVerifyProfileByEmail(
            String email, ResultMatcher expectedStatus
    ) throws Exception {
        mockMvc.perform(get("/api/v1/search/search-profile-email/"+email)
                .header("Authorization","serial eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJlbW1veDU1QGdtYWlsLmNvbSIsImV4cCI6MTY1NzY1NjI0Nn0.xOCakRQLFNXqbSOI3b3jsFek5ybfOmdMCfZ71N1TQ2o")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(expectedStatus);
    }

    private void searchAndVerifyProfileOrganisationName(
            String organisationName, ResultMatcher expectedStatus
    ) throws Exception {
        mockMvc.perform(get("/api/v1/search/search-profile-organisationName/"+ organisationName)
                .header("Authorization","serial eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJlbW1veDU1QGdtYWlsLmNvbSIsImV4cCI6MTY1NzY1NjI0Nn0.xOCakRQLFNXqbSOI3b3jsFek5ybfOmdMCfZ71N1TQ2o")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(expectedStatus);
    }

    private void seedData() {

        user.setEmail("mike@app.com");
        user.setFirstName("Mike");
        user.setPhoneNumber("0029934");
        user.setReferenceCode("CRT");
        user.setSurname("Ang");
        user.setDateCreated(LocalDateTime.now());
        user.setAccountStatus(1);
        String fullName = String.format("%s %s", user.getFirstName(), user.getSurname());
        user.setName(fullName);
        Users regUser;
        if(userRepository.existsByEmail(user.getEmail()) || userRepository.existsByPhoneNumber(user.getEmail()))
            regUser = user;
        else
            regUser = userRepository.save(user);

        //personal profile 1
        Profile profile = new Profile();
        profile.setGender("male");
        profile.setPhoneNumber("091233");
        profile.setEmail("codedaa@app.com");
        profile.setFirstName("appp");
        profile.setSurname("appp");
        profile.setState("state");
        profile.setCorporate(false);
        profile.setUserId(String.valueOf(regUser.getId()));
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

    public String generateToken(String userName) {
        try {
            System.out.println("::::::GENERATE TOKEN:::::");
            String token = Jwts.builder().setSubject(userName)
                    .setExpiration(new Date(System.currentTimeMillis() + JWT_TOKEN_VALIDITY * 1000))
                    .signWith(SignatureAlgorithm.HS512, SECRET_TOKEN).compact();
            System.out.println(":::::Token:::::");
            return TOKEN_PREFIX + token;
        } catch (Exception e) {
            throw new RuntimeException(e.fillInStackTrace());
        }
    }


}

