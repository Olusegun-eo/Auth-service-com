package com.waya.wayaauthenticationservice.integration;

import static com.waya.wayaauthenticationservice.util.Constant.JWT_TOKEN_VALIDITY;
import static com.waya.wayaauthenticationservice.util.Constant.SECRET_TOKEN;
import static com.waya.wayaauthenticationservice.util.Constant.TOKEN_PREFIX;
import static com.waya.wayaauthenticationservice.util.JsonString.asJsonString;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Optional;

import org.hamcrest.core.Is;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;


import com.waya.wayaauthenticationservice.entity.OtherDetails;
import com.waya.wayaauthenticationservice.entity.Profile;
import com.waya.wayaauthenticationservice.entity.ReferralCode;
import com.waya.wayaauthenticationservice.entity.Users;
import com.waya.wayaauthenticationservice.enums.DeleteType;
import com.waya.wayaauthenticationservice.pojo.others.CorporateProfileRequest;
import com.waya.wayaauthenticationservice.pojo.others.DeleteRequest;
import com.waya.wayaauthenticationservice.pojo.others.PersonalProfileRequest;
import com.waya.wayaauthenticationservice.pojo.others.UpdateCorporateProfileRequest;
import com.waya.wayaauthenticationservice.pojo.others.UpdatePersonalProfileRequest;
import com.waya.wayaauthenticationservice.proxy.FileResourceServiceFeignClient;
import com.waya.wayaauthenticationservice.repository.ProfileRepository;
import com.waya.wayaauthenticationservice.repository.ReferralCodeRepository;
import com.waya.wayaauthenticationservice.repository.RolesRepository;
import com.waya.wayaauthenticationservice.repository.UserRepository;
import com.waya.wayaauthenticationservice.util.Constant;
import com.waya.wayaauthenticationservice.util.TestHelper;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.transaction.annotation.Transactional;

@ActiveProfiles("test")
@SpringBootTest(properties = {"eureka.client.enabled=false"})
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DirtiesContext
@EmbeddedKafka(partitions = 1, brokerProperties =
        {"listeners=PLAINTEXT://localhost:9092", "port=9092"})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Transactional
class ProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private ReferralCodeRepository referralCodeRepository;

    @Autowired
    private UserRepository userRepository;

    //@Autowired
    //private OtherDetailsRepository otherDetailsRepository;

    @MockBean
    private FileResourceServiceFeignClient fileResourceServiceFeignClient;

    //private final Profile profilePersonal = new Profile();
    //private Profile profile = new Profile();

    final MockMultipartFile file = new MockMultipartFile("files",
            "snapshot.png", MediaType.IMAGE_JPEG_VALUE, "content".getBytes(StandardCharsets.UTF_8));

//    final String setUpUserId = "749";
//    private final String EMAIL = "app@app.com";
//    private final String EMAIL2 = "cpd@app.com";

    @Autowired
    private RolesRepository rolesRepository;

    private Users user = new Users();
    private Users user2 = new Users();

    private TestHelper testHelper;
    private ReferralCode referralCode;

    @BeforeAll
    void setUp() {
        user2 = createCorporateUser();
        testHelper = new TestHelper(userRepository, rolesRepository);
        user = testHelper.createTestUser();
    }

    @BeforeEach
    void beforeEach() {
        Optional<Profile> profile = profileRepository
                .findByUserId(false, user.getId().toString());
        if (profile.isPresent()){
            System.out.println("profile :::::::: " + profile.get());
            profileRepository.delete(profile.get());
        }
        Optional<Profile> profile2 = profileRepository
                .findByUserId(false, user2.getId().toString());
        if (profile2.isPresent()){
            System.out.println("profile 2 :::::::: " + profile2.get());
            profileRepository.delete(profile.get());
        }
    }

    @Order(1)
    @Test
    @DisplayName("create personal profile successfully 💯")
    void createPersonalProfile() throws Exception {
        final PersonalProfileRequest personalProfileRequest = setPersonalProfileData(
                "kola@app.com", user.getId().toString(), "2340291838294");

        createAndVerifyPersonalProfile(personalProfileRequest, "$.message",
                "profile created. An OTP has been sent to your phone",
                "$.httpStatus", "OK");
    }

    @Order(2)
    @Test
    @DisplayName("create personal profile when email already exist 🔥")
    void createPersonalProfileWhenProfileWithEmailExist() throws Exception {
        final PersonalProfileRequest personalProfileRequest = setPersonalProfileData(
                "kola@app.com", user.getId().toString(), "2340291838294");

        createAndVerifyPersonalProfile(personalProfileRequest, "$.message",
                "profile created. An OTP has been sent to your phone",
                "$.httpStatus", "OK");

        final PersonalProfileRequest newPersonalProfileRequest = setPersonalProfileData(
                "app@app.com", user.getId().toString(), "2340291838294");

        createAndVerifyPersonalProfile(newPersonalProfileRequest, "$.message",
                "Profile with Provided User ID already Exists",
                "$.httpStatus", "BAD_REQUEST");
    }

    @Order(3)
    @Test
    @DisplayName("create personal profile when userId already exist 🔥")
    void createPersonalProfileWhenUserIdExist() throws Exception {

        final PersonalProfileRequest personalProfileRequest = setPersonalProfileData(
                "kola@app.com", user.getId().toString(), "2340291838294");

        createAndVerifyPersonalProfile(personalProfileRequest, "$.message",
                "profile created. An OTP has been sent to your phone",
                "$.httpStatus", "OK");

        final PersonalProfileRequest newPersonalProfileRequest = setPersonalProfileData(
                "app@app12.com", user.getId().toString(), "2340291838294");

        createAndVerifyPersonalProfile(newPersonalProfileRequest, "$.message",
                "Profile with Provided User ID already Exists",
                "$.httpStatus", "BAD_REQUEST");
    }

    @Order(4)
    @Test
    @DisplayName("create corporate profile successfully 💯")
    void createCorporateProfile() throws Exception {
        final CorporateProfileRequest corporateProfileRequest = setCorporateProfileData(
                "rt@app.com", user2.getId().toString(),"2349870928349");

        createAndVerifyCorporateProfile(corporateProfileRequest, "$.message",
                "profile created. An OTP has been sent to your phone",
                "$.httpStatus", "OK");
    }

    @Order(5)
    @Test
    @DisplayName("create corporate profile when User Id does not exist 💯")
    void createCorporateProfileWhenUserIdDoesNotExist() throws Exception {
        final CorporateProfileRequest corporateProfileRequest = setCorporateProfileData(
                "app@app.com", "5432","2349870928349");

        createAndVerifyCorporateProfile(corporateProfileRequest, "$.message",
                "Base User with Provided ID not Found",
                "$.httpStatus", "BAD_REQUEST");
    }

    @Order(6)
    @Test
    @DisplayName("create corporate profile when user id exist 💯")
    void createCorporateProfileWhenUserIdExist() throws Exception {
        final CorporateProfileRequest corporateProfileRequest = setCorporateProfileData(
                "rt@app.com", user2.getId().toString(),"2349870928349");

        createAndVerifyCorporateProfile(corporateProfileRequest, "$.message",
                "profile created. An OTP has been sent to your phone",
                "$.httpStatus", "OK");

        final CorporateProfileRequest newCorporateProfileRequest = setCorporateProfileData(
                "app@app.com", user2.getId().toString(),"2349870928349");

        createAndVerifyCorporateProfile(newCorporateProfileRequest, "$.message",
                "Profile with Provided User ID already Exists",
                "$.httpStatus", "BAD_REQUEST");
    }

    @Order(7)
    @Test
    @DisplayName("get a users profile")
    void getPersonalProfile() throws Exception {
        final PersonalProfileRequest personalProfileRequest = setPersonalProfileData(
                "kola@app.com", user.getId().toString(),"2349870928349");

        createAndVerifyPersonalProfile(personalProfileRequest, "$.message",
                "profile created. An OTP has been sent to your phone",
                "$.httpStatus", "OK");

        getAndVerifyUserProfile(user.getId().toString(), "$.message",
                "retrieved successfully", status().isOk());
    }

    @Order(7)
    @Test
    @DisplayName("get a corporate users profile")
    void getCorporateProfile() throws Exception {
        final CorporateProfileRequest corporateProfileRequest = setCorporateProfileData(
                "rt@app.com", user2.getId().toString(),"2349870928349");

        createAndVerifyCorporateProfile(corporateProfileRequest, "$.message",
                "profile created. An OTP has been sent to your phone",
                "$.httpStatus", "OK");

        getAndVerifyUserProfile(user2.getId().toString(), "$.message",
                "retrieved successfully", status().isOk());
    }

    @Order(8)
    @Test
    @DisplayName("get a users profile when profile does not exist")
    void getUsersProfileWhenProfileNotExist() throws Exception {
        getAndVerifyUserProfile("2iu24", "$.message",
                "profile with that user id is not found", status().isBadRequest());
    }

    @Order(9)
    @Test
    @DisplayName("update a personal profile successfully")
    void updatePersonalProfile() throws Exception {

        final PersonalProfileRequest personalProfileRequest = setPersonalProfileData(
                "kola@app.com", user.getId().toString(),"2349870928349");

        createAndVerifyPersonalProfile(personalProfileRequest, "$.message",
                "profile created. An OTP has been sent to your phone",
                "$.httpStatus", "OK");

        final UpdatePersonalProfileRequest updatePersonalProfileRequest =
                setUpdatePersonalProfileRequest("kola@app.com","2349870928349");

        updateAndVerifyPersonalProfile(updatePersonalProfileRequest, user.getId().toString(), "$.message",
                "profile updated successfully", status().isCreated());
    }

    @Order(10)
    @Test
    @DisplayName("update a personal profile when user id is invalid")
    void updatePersonalProfileWithInValidUserId() throws Exception {

        final UpdatePersonalProfileRequest updatePersonalProfileRequest =
                setUpdatePersonalProfileRequest("kola@app.com","2349870928349");

        updateAndVerifyPersonalProfile(updatePersonalProfileRequest, "121233", "$.message",
                Constant.PROFILE_NOT_EXIST, status().isBadRequest());
    }

    @Order(11)
    @Test
    @DisplayName("update a corporate profile successfully")
    void updateCorporateProfile() throws Exception {
        final CorporateProfileRequest corporateProfileRequest = setCorporateProfileData(
                "rt@app.com", user2.getId().toString(),"2349870928349");

        createAndVerifyCorporateProfile(corporateProfileRequest, "$.message",
                "profile created. An OTP has been sent to your phone",
                "$.httpStatus", "OK");

        final UpdateCorporateProfileRequest updateCorporateProfileRequest =
                setUpdateCorporateProfileRequest("kola@app.com","2349870928349");

        updateAndVerifyCorporateProfile(updateCorporateProfileRequest, user2.getId().toString(), "$.message",
                "profile updated successfully", status().isCreated());
    }

    @Order(12)
    @Test
    @DisplayName("update a corporate profile when user id is not found")
    void updateCorporateProfileWithInvalidUserId() throws Exception {
        final UpdateCorporateProfileRequest updateCorporateProfileRequest =
                setUpdateCorporateProfileRequest("kola@app.com","2349870928349");

        updateAndVerifyCorporateProfile(updateCorporateProfileRequest, "4242", "$.message",
                "user with that id not found or is not a Corporate User", status().isUnprocessableEntity());
    }

    @Order(13)
    @Test
    @DisplayName("get all users referrals successfully")
    void getAllUsersReferral() throws Exception {
        final CorporateProfileRequest corporateProfileRequest = setCorporateProfileData(
                "rt@app.com", user2.getId().toString(),"2349870928349");

        createAndVerifyCorporateProfile(corporateProfileRequest, "$.message",
                "profile created. An OTP has been sent to your phone",
                "$.httpStatus", "OK");

        getAndVerifyAllUsersReferrals(user2.getId().toString(), status().isOk());
    }

    @Order(14)
    @Test
    @DisplayName("delete personal profile  💯")
    void deleteProfile() throws Exception {

        final PersonalProfileRequest personalProfileRequest = setPersonalProfileData(
                "tella@app.com", user.getId().toString(), "2340291838294");

        createAndVerifyPersonalProfile(personalProfileRequest, "$.message",
                "profile created. An OTP has been sent to your phone",
                "$.httpStatus", "OK");

        DeleteRequest deleteRequest = DeleteRequest.builder()
                .deleteType(DeleteType.DELETE).userId(user.getId().toString()).build();

        deleteProfile(deleteRequest, "$.message",
                "Deletion successful",
                "$.code", "200");
    }

    @Order(15)
    @Test
    @DisplayName("invalid delete personal profile  💯")
    void invalidDeleteProfile() throws Exception {

        final PersonalProfileRequest personalProfileRequest = setPersonalProfileData(
                "youtbue@app.com", user.getId().toString(), "2340291838294");

        createAndVerifyPersonalProfile(personalProfileRequest, "$.message",
                "profile created. An OTP has been sent to your phone",
                "$.httpStatus", "OK");

        DeleteRequest deleteRequest = DeleteRequest.builder()
                .deleteType(DeleteType.NONE).userId("455").build();

        deleteProfile(deleteRequest, "$.error",
                "Invalid delete type, try RESTORE OR DELETE",
                "$.code", "401");
    }

    @Order(16)
    @Test
    @DisplayName("Restore personal profile  💯")
    void restoreProfile() throws Exception {

        final PersonalProfileRequest personalProfileRequest = setPersonalProfileData(
                "benthly@app.com", user.getId().toString(), "2340291838294");

        createAndVerifyPersonalProfile(personalProfileRequest, "$.message",
                "profile created. An OTP has been sent to your phone",
                "$.httpStatus", "OK");

        DeleteRequest deleteRequest = DeleteRequest.builder()
                .deleteType(DeleteType.DELETE).userId(user.getId().toString()).build();

        deleteProfile(deleteRequest, "$.message",
                "Deletion successful",
                "$.code", "200");

        DeleteRequest restoreRequest = DeleteRequest.builder()
                .deleteType(DeleteType.RESTORE).userId( user.getId().toString()).build();

        deleteProfile(restoreRequest, "$.message",
                "Profile has been restored",
                "$.code", "200");
    }

    @Order(17)
    @Test
    @DisplayName("delete personal profile error 💯")
    void deleteProfileError() throws Exception {

        DeleteRequest deleteRequest = DeleteRequest.builder()
                .deleteType(DeleteType.DELETE).userId("10").build();

        deleteProfile(deleteRequest, "$.error",
                "Profile with userId do not exist or already deleted",
                "$.code", "300");
    }

    @Order(18)
    @Test
    @DisplayName("Restore personal profile error 💯")
    void restoreProfileError() throws Exception {

        DeleteRequest deleteRequest = DeleteRequest.builder()
                .deleteType(DeleteType.RESTORE).userId("10").build();

        deleteProfile(deleteRequest, "$.error",
                "Profile with userId do not exist or already restored",
                "$.code", "300");
    }

    private void createAndVerifyPersonalProfile(
            final PersonalProfileRequest personalProfileRequest,
            final String jsonPath0, final String jsonPathMessage0,
            final String jsonPath1, final String jsonPathMessage1
    ) throws Exception {
        mockMvc.perform(post("/api/v1/profile/personal-profile")
                .header("Authorization", generateToken(user))
                .contentType(APPLICATION_JSON)
                .content(asJsonString(personalProfileRequest)))
                .andExpect(jsonPath(jsonPath0, Is.is(jsonPathMessage0)))
                .andExpect(jsonPath(jsonPath1, Is.is(jsonPathMessage1)));
    }

    private void deleteProfile(
            final DeleteRequest deleteRequest,
            final String jsonPath0, final String jsonPathMessage0,
            final String jsonPath1, final String jsonPathMessage1

    ) throws Exception {

        mockMvc.perform(put("/api/v1/profile/delete-restore")
                .header("Authorization", generateToken(user))
                .contentType(APPLICATION_JSON)
                .content(asJsonString(deleteRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(jsonPath0, Is.is(jsonPathMessage0)))
                .andExpect(jsonPath(jsonPath1, Is.is(jsonPathMessage1)));
    }

    private void createAndVerifyCorporateProfile(
            final CorporateProfileRequest corporateProfileRequest,
            final String jsonPath0, final String jsonPathMessage0,
            final String jsonPath1, final String jsonPathMessage1
    ) throws Exception {
        mockMvc.perform(post("/api/v1/profile/corporate-profile")
                .header("Authorization", generateToken(user))
                .contentType(APPLICATION_JSON)
                .content(asJsonString(corporateProfileRequest)))
                .andExpect(jsonPath(jsonPath0, Is.is(jsonPathMessage0))).andDo(print())
                .andExpect(jsonPath(jsonPath1, Is.is(jsonPathMessage1)));
    }

    private void getAndVerifyUserProfile(
            final String userId, final String jsonPath,
            final String jsonPathMessage,
            ResultMatcher expectedStatus
    ) throws Exception {
        mockMvc.perform(get("/api/v1/profile/" + userId)
                .header("Authorization", generateToken(user))
                .contentType(APPLICATION_JSON))
                .andExpect(expectedStatus)
                .andExpect(jsonPath(jsonPath, Is.is(jsonPathMessage)))
                .andDo(print());
    }

    private void updateAndVerifyPersonalProfile(
            final UpdatePersonalProfileRequest profileRequest,
            final String userId, final String jsonPath,
            final String jsonPathMessage, ResultMatcher expectedStatus
    ) throws Exception {
        mockMvc.perform(put("/api/v1/profile/update-personal-profile/" + userId)
                .header("Authorization", generateToken(user))
                .contentType(APPLICATION_JSON)
                .content(asJsonString(profileRequest)))
                .andExpect(expectedStatus)
                .andExpect(jsonPath(jsonPath, Is.is(jsonPathMessage)));
    }

    private void updateAndVerifyCorporateProfile(
            final UpdateCorporateProfileRequest corporateProfileRequest,
            final String userId, final String jsonPath,
            final String jsonPathMessage, ResultMatcher expectedStatus
    ) throws Exception {
        mockMvc.perform(put("/api/v1/profile/update-corporate-profile/" + userId)
                .header("Authorization", generateToken(user))
                .contentType(APPLICATION_JSON)
                .content(asJsonString(corporateProfileRequest)))
                .andExpect(expectedStatus)
                .andExpect(jsonPath(jsonPath, Is.is(jsonPathMessage)));
    }

    private void getAndVerifyAllUsersReferrals(
            final String userId, ResultMatcher expectedStatus
    ) throws Exception {
        mockMvc.perform(get("/api/v1/profile/user-referrals/" + userId)
                .header("Authorization", generateToken(user))
                .contentType(APPLICATION_JSON))
                .andExpect(expectedStatus);
    }

    private PersonalProfileRequest setPersonalProfileData(
            final String email, String userId, String phoneNumber
    ) {
        final PersonalProfileRequest personalProfileRequest
                = new PersonalProfileRequest();
        personalProfileRequest.setFirstName("Omar");
        personalProfileRequest.setSurname("Bawa");
        personalProfileRequest.setEmail(email);
        personalProfileRequest.setUserId(userId);
        personalProfileRequest.setPhoneNumber(phoneNumber);

        return personalProfileRequest;
    }

    private CorporateProfileRequest setCorporateProfileData(
            final String email, final String userId, String phoneNumber
    ) {

        final CorporateProfileRequest corporateProfileRequest
                = new CorporateProfileRequest();
        corporateProfileRequest.setFirstName("firstname");
        corporateProfileRequest.setSurname("surname");
        corporateProfileRequest.setBusinessType("businessType");
        corporateProfileRequest.setOrganisationType("organisationType");
        corporateProfileRequest.setEmail(email);
        corporateProfileRequest.setOrganisationEmail("app20@app.com");
        corporateProfileRequest.setOrganisationName("organisation name");
        corporateProfileRequest.setPhoneNumber(phoneNumber);
        corporateProfileRequest.setUserId(userId);

        return corporateProfileRequest;
    }

    private UpdatePersonalProfileRequest setUpdatePersonalProfileRequest(String email, String phoneNumber) {
        final UpdatePersonalProfileRequest updatePersonalProfileRequest =
                new UpdatePersonalProfileRequest();

        updatePersonalProfileRequest.setEmail(email);
        updatePersonalProfileRequest.setFirstName("updated first name");
        updatePersonalProfileRequest.setSurname("updated surname");
        updatePersonalProfileRequest.setPhoneNumber(phoneNumber);
        updatePersonalProfileRequest.setMiddleName("update middle name");

        return updatePersonalProfileRequest;
    }

    private UpdateCorporateProfileRequest setUpdateCorporateProfileRequest(String email, String phoneNumber) {
        final UpdateCorporateProfileRequest updateCorporateProfileRequest =
                new UpdateCorporateProfileRequest();

        updateCorporateProfileRequest.setBusinessType("updated business type");
        updateCorporateProfileRequest.setCity("city");
        updateCorporateProfileRequest.setFirstName("first name updated");
        updateCorporateProfileRequest.setPhoneNumber(phoneNumber);
        updateCorporateProfileRequest.setSurname("surname updated");
        updateCorporateProfileRequest.setGender("Male");
        updateCorporateProfileRequest.setEmail(email);
        updateCorporateProfileRequest.setOrganisationName("organisation name updated");
        updateCorporateProfileRequest.setOrganisationType("organisation type");
        updateCorporateProfileRequest.setBusinessType("business type");
        updateCorporateProfileRequest.setState("state");
        updateCorporateProfileRequest.setOrganisationEmail("cpd@app.com");

        return updateCorporateProfileRequest;
    }

    private Users createCorporateUser() {
        Users user = new Users();
        user.setActive(true);
        user.setCorporate(true);
        user.setName("firstName SurName");
        user.setFirstName("FirstName");
        user.setSurname("Surname");
        user.setPassword("test@123");
        user.setEmail("cpd@app.com");
        user.setPhoneNumber("23401010110109");
        if(!(userRepository.existsByEmail(user.getEmail()) || userRepository.existsByPhoneNumber(user.getEmail())))
            user = userRepository.save(user);

        return user;
    }

    public String generateToken(Users user) {
        try {
            System.out.println("::::::GENERATE TOKEN:::::");
            String token = Jwts.builder().setSubject(user.getEmail())
                    .setExpiration(new Date(System.currentTimeMillis() + JWT_TOKEN_VALIDITY * 1000))
                    .signWith(SignatureAlgorithm.HS512, SECRET_TOKEN).compact();
            System.out.println(":::::Token:::::");
            return TOKEN_PREFIX + token;
        } catch (Exception e) {
            throw new RuntimeException(e.fillInStackTrace());
        }
    }

}

