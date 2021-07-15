package com.waya.wayaauthenticationservice.integration;

import com.waya.wayaauthenticationservice.entity.Users;
import com.waya.wayaauthenticationservice.pojo.others.LoginDetailsPojo;
import com.waya.wayaauthenticationservice.pojo.userDTO.BaseUserPojo;
import com.waya.wayaauthenticationservice.pojo.userDTO.CorporateUserPojo;
import com.waya.wayaauthenticationservice.repository.UserRepository;
import kafka.utils.PasswordEncoder;
import org.hamcrest.core.Is;
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

import static com.waya.wayaauthenticationservice.util.JsonString.asJsonString;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AuthenticationControllerTest {

    Users user = new Users();

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    BCryptPasswordEncoder passwordEncoder;

    @BeforeAll
    public void setUp() {
        user.setEmail("stan@toju.com");
        user.setFirstName("Stan");
        user.setSurname("Toju");
        user.setActive(true);
        user.setPhoneNumber("2348166302445");
        user.setPassword(passwordEncoder.encode("test@123"));
        user.setName(String.format("%s %s", user.getFirstName(), user.getSurname()));
        user.setId(1l);

        userRepository.save(user);
    }

    @Test
    @DisplayName("create personal profile successfully")
    public void createPersonalUserSuccessfully() throws Exception {
        BaseUserPojo user = new BaseUserPojo();
        user.setEmail("emmox55@gmail.com");
        user.setFirstName("Stan");
        user.setSurname("Toju");
        user.setPhoneNumber("2348104728022");
        user.setPassword("test@123");

        createNewUser(user, "$.message", "Successful", status().isCreated());
    }

    @Test
    @DisplayName("create personal profile with Validation Issues")
    public void createPersonalUserWithValidationIssues() throws Exception {
        BaseUserPojo user = new BaseUserPojo();
        user.setEmail("stan-toju.com"); // wrong-email
        user.setFirstName("Stan");
        user.setSurname("Toju");
        user.setPhoneNumber("2368166302445");
        user.setPassword("test@123");

        createNewUser(user, "$.message", "Validation Errors", status().isBadRequest());
    }

    @Test
    @DisplayName("create corporate profile successfully")
    public void createCorpUserSuccessfully() throws Exception {
        CorporateUserPojo user = new CorporateUserPojo();
        user.setEmail("micro@toju.com");
        user.setFirstName("Stan");
        user.setSurname("Toju");
        user.setPhoneNumber("2347030355396");
        user.setPassword("test@123");
        user.setCity("Shomolu");
        user.setBusinessType("Banking");
        user.setOfficeAddress("MaryLand Avenue");
        user.setState("Lagos");
        user.setOrgName("Fidelity Bank");
        user.setOrgEmail("mico-toju@fidelitybank.ng");
        user.setOrgPhone("2345678990001");
        user.setOrgType("Banking and Finance");

        createCorporateNewUser(user, "$.message", "Successful", status().isCreated());
    }

    @Test
    @DisplayName("create corporate profile with Validation Issues")
    public void createCorpUserWithValidationIssues() throws Exception {
        CorporateUserPojo user = new CorporateUserPojo();
        user.setEmail("stan-toju.com"); // wrong-email
        user.setFirstName("Stan");
        user.setSurname("Toju");
        user.setPhoneNumber("2368166302445"); // Wrong Phone Number
        user.setPassword("test@123");

        createCorporateNewUser(user, "$.message", "Validation Errors", status().isBadRequest());
    }

    @Test
    @DisplayName("Login Successfully with PhoneNumber and Password")
    public void loginWithEmailSuccessfully() throws Exception {
        LoginDetailsPojo loginUser = new LoginDetailsPojo();
        loginUser.setEmailOrPhoneNumber(user.getEmail());
        loginUser.setPassword("test@123");
        loginNewUser(loginUser, "$.message", "Login Successful", status().isOk());
    }

    @Test
    @DisplayName("Login Successfully with Email and Password")
    public void loginWithPhoneSuccessfully() throws Exception {
        LoginDetailsPojo loginUser = new LoginDetailsPojo();
        loginUser.setEmailOrPhoneNumber(user.getPhoneNumber());
        loginUser.setPassword("test@123");
        loginNewUser(loginUser, "$.message", "Login Successful", status().isOk());
    }

    private void createNewUser(
            BaseUserPojo userPojo, String jsonPath,
            String jsonPathMessage, ResultMatcher expectedStatus)
            throws Exception {
        mockMvc.perform(post("/api/v1/auth/create")
                .accept("application/json")
                .contentType("application/json")
                .content(asJsonString(userPojo)))
                .andExpect(expectedStatus)
                .andExpect(jsonPath(jsonPath, Is.is(jsonPathMessage)));
    }

    private void createCorporateNewUser(CorporateUserPojo userPojo, String jsonPath, String jsonPathMessage, ResultMatcher expectedStatus) throws Exception {
        mockMvc.perform(post("/api/v1/auth/create-corporate")
                .accept("application/json")
                .contentType(APPLICATION_JSON)
                .content(asJsonString(userPojo)))
                .andExpect(expectedStatus)
                .andExpect(jsonPath(jsonPath, Is.is(jsonPathMessage)));
    }

    //Login Successful
    private void loginNewUser(LoginDetailsPojo userPojo, String jsonPath, String jsonPathMessage, ResultMatcher expectedStatus) throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                .accept("application/json")
                .contentType(APPLICATION_JSON)
                .content(asJsonString(userPojo)))
                .andExpect(expectedStatus)
                .andExpect(jsonPath(jsonPath, Is.is(jsonPathMessage)));
    }

}