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

import org.hamcrest.core.Is;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
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

@ActiveProfiles("test")
@SpringBootTest(properties = {"eureka.client.enabled=false"})
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DirtiesContext
@EmbeddedKafka(partitions = 1, brokerProperties =
        {"listeners=PLAINTEXT://localhost:9092", "port=9092"})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
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

    private final Profile profilePersonal = new Profile();
    private final Profile profile = new Profile();

    final MockMultipartFile file = new MockMultipartFile("files",
            "snapshot.png", MediaType.IMAGE_JPEG_VALUE, "content".getBytes(StandardCharsets.UTF_8));

    //private final ProfileImageResponse profileImageResponse =
    //        new ProfileImageResponse("image url");

    //private final ApiResponse<ProfileImageResponse> apiResponse =
    //        new ApiResponse<>(profileImageResponse, "success", true);

    final String setUpUserId = "uew748";


    @Autowired
    private RolesRepository rolesRepository;

    private Users user = new Users();

    private TestHelper testHelper;

    @BeforeAll
    void setUp() {
        testHelper = new TestHelper(userRepository, rolesRepository);
        user = testHelper.createTestUser();
        seedData();
    }

    @Order(1)
    @Test
    @DisplayName("create personal profile successfully 💯")
    void createPersonalProfile() throws Exception {
        final PersonalProfileRequest personalProfileRequest = setPersonalProfileData(
                "kola@app.com", "244");

        createAndVerifyPersonalProfile(personalProfileRequest, "$.message",
                "profile created. An OTP has been sent to your phone",
                "$.httpStatus", "OK");
    }

    @Order(2)
    @Test
    @DisplayName("create personal profile when email already exist 🔥")
    void createPersonalProfileWhenProfileWithEmailExist() throws Exception {
        final PersonalProfileRequest personalProfileRequest = setPersonalProfileData(
                "app@app.com", "2ewe");

        createAndVerifyPersonalProfile(personalProfileRequest, "$.message",
                Constant.DUPLICATE_KEY,
                "$.httpStatus", "UNPROCESSABLE_ENTITY");
    }

    @Order(3)
    @Test
    @DisplayName("create personal profile when userId already exist 🔥")
    void createPersonalProfileWhenUserIdExist() throws Exception {
        final PersonalProfileRequest personalProfileRequest = setPersonalProfileData(
                "app@app12.com", "123");

        createAndVerifyPersonalProfile(personalProfileRequest, "$.message",
                "duplicate key exception, user id or email might already exist",
                "$.httpStatus", "UNPROCESSABLE_ENTITY");
    }

    @Order(4)
    @Test
    @DisplayName("create corporate profile successfully 💯")
    void createCorporateProfile() throws Exception {
        final CorporateProfileRequest corporateProfileRequest = setCorporateProfileData(
                "rt@app.com", "5432");

        createAndVerifyCorporateProfile(corporateProfileRequest, "$.message",
                "profile created. An OTP has been sent to your phone",
                "$.httpStatus", "OK");
    }

    @Order(5)
    @Test
    @DisplayName("create corporate profile when email exist 💯")
    void createCorporateProfileWhenEmailExist() throws Exception {
        final CorporateProfileRequest corporateProfileRequest = setCorporateProfileData(
                "app@app.com", "5432");

        createAndVerifyCorporateProfile(corporateProfileRequest, "$.message",
                Constant.DUPLICATE_KEY,
                "$.httpStatus", "UNPROCESSABLE_ENTITY");
    }

    @Order(6)
    @Test
    @DisplayName("create corporate profile when user id exist 💯")
    void createCorporateProfileWhenUserIdExist() throws Exception {
        final CorporateProfileRequest corporateProfileRequest = setCorporateProfileData(
                "app@app.com", "123");

        createAndVerifyCorporateProfile(corporateProfileRequest, "$.message",
                Constant.DUPLICATE_KEY,
                "$.httpStatus", "UNPROCESSABLE_ENTITY");
    }

    @Order(7)
    @Test
    @DisplayName("get a users profile")
    void getPersonalProfile() throws Exception {
        getAndVerifyUserProfile("123", "$.message",
                "retrieved successfully", status().isOk());
    }

    @Order(7)
    @Test
    @DisplayName("get a corporate users profile")
    void getCorporateProfile() throws Exception {
        getAndVerifyUserProfile("42", "$.message",
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

        final UpdatePersonalProfileRequest updatePersonalProfileRequest = setUpdatePersonalProfileRequest();

        updateAndVerifyPersonalProfile(updatePersonalProfileRequest, "1234", "$.message",
                "profile updated successfully", status().isCreated());
    }

    @Order(10)
    @Test
    @DisplayName("update a personal profile when user id is invalid")
    void updatePersonalProfileWithInValidUserId() throws Exception {

        final UpdatePersonalProfileRequest updatePersonalProfileRequest = setUpdatePersonalProfileRequest();

        updateAndVerifyPersonalProfile(updatePersonalProfileRequest, "121233", "$.message",
                Constant.PROFILE_NOT_EXIST, status().isBadRequest());
    }

    @Order(11)
    @Test
    @DisplayName("update a corporate profile successfully")
    void updateCorporateProfile() throws Exception {
        final UpdateCorporateProfileRequest updateCorporateProfileRequest = setUpdateCorporateProfileRequest();

        updateAndVerifyCorporateProfile(updateCorporateProfileRequest, "42", "$.message",
                "profile updated successfully", status().isCreated());
    }

    @Order(12)
    @Test
    @DisplayName("update a corporate profile when user id is not found")
    void updateCorporateProfileWithInvalidUserId() throws Exception {
        final UpdateCorporateProfileRequest updateCorporateProfileRequest = setUpdateCorporateProfileRequest();

        updateAndVerifyCorporateProfile(updateCorporateProfileRequest, "4242", "$.message",
                "user with that id not found", status().isUnprocessableEntity());
    }

    @Order(13)
    @Test
    @DisplayName("get all users referrals successfully")
    void getAllUsersReferral() throws Exception {
        getAndVerifyAllUsersReferrals(profile.getUserId(), status().isOk());
    }

    @Order(14)
    @Test
    @DisplayName("delete personal profile  💯")
    void deleteProfile() throws Exception {

        final PersonalProfileRequest personalProfileRequest = setPersonalProfileData(
                "tella@app.com", "338");

        createAndVerifyPersonalProfile(personalProfileRequest, "$.message",
                "profile created. An OTP has been sent to your phone",
                "$.httpStatus", "OK");

        DeleteRequest deleteRequest = DeleteRequest.builder()
                .deleteType(DeleteType.DELETE).userId("338").build();

        deleteProfile(deleteRequest, "$.message",
                "Deletion successful",
                "$.code", "200");
    }

    @Order(15)
    @Test
    @DisplayName("invalid delete personal profile  💯")
    void invalidDeleteProfile() throws Exception {

        final PersonalProfileRequest personalProfileRequest = setPersonalProfileData(
                "youtbue@app.com", "455");

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
                "benthly@app.com", "199");

        createAndVerifyPersonalProfile(personalProfileRequest, "$.message",
                "profile created. An OTP has been sent to your phone",
                "$.httpStatus", "OK");

        DeleteRequest deleteRequest = DeleteRequest.builder()
                .deleteType(DeleteType.DELETE).userId("199").build();

        deleteProfile(deleteRequest, "$.message",
                "Deletion successful",
                "$.code", "200");

        DeleteRequest restoreRequest = DeleteRequest.builder()
                .deleteType(DeleteType.RESTORE).userId("199").build();

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
            final String email, String userId
    ) {
        final PersonalProfileRequest personalProfileRequest
                = new PersonalProfileRequest();
        personalProfileRequest.setFirstName("Omar");
        personalProfileRequest.setSurname("Bawa");
        personalProfileRequest.setEmail(email);
        personalProfileRequest.setUserId(userId);
        personalProfileRequest.setPhoneNumber("0291838294");

        return personalProfileRequest;
    }

    private CorporateProfileRequest setCorporateProfileData(
            final String email, final String userId
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
        corporateProfileRequest.setPhoneNumber("092834");
        corporateProfileRequest.setUserId(userId);

        return corporateProfileRequest;
    }

    private UpdatePersonalProfileRequest setUpdatePersonalProfileRequest() {
        final UpdatePersonalProfileRequest updatePersonalProfileRequest =
                new UpdatePersonalProfileRequest();

        updatePersonalProfileRequest.setEmail("app@app.com");
        updatePersonalProfileRequest.setFirstName("updated first name");
        updatePersonalProfileRequest.setSurname("updated surname");
        updatePersonalProfileRequest.setPhoneNumber("update number");
        updatePersonalProfileRequest.setMiddleName("update middle name");

        return updatePersonalProfileRequest;
    }

    private UpdateCorporateProfileRequest setUpdateCorporateProfileRequest() {
        final UpdateCorporateProfileRequest updateCorporateProfileRequest =
                new UpdateCorporateProfileRequest();

        updateCorporateProfileRequest.setBusinessType("updated business type");
        updateCorporateProfileRequest.setCity("city");
        updateCorporateProfileRequest.setFirstName("first name updated");
        updateCorporateProfileRequest.setPhoneNumber("001983");
        updateCorporateProfileRequest.setSurname("surname updated");
        updateCorporateProfileRequest.setGender("Male");
        updateCorporateProfileRequest.setOrganisationName("organisation name updated");
        updateCorporateProfileRequest.setOrganisationType("organisation type");
        updateCorporateProfileRequest.setBusinessType("business type");
        updateCorporateProfileRequest.setState("state");
        updateCorporateProfileRequest.setOrganisationEmail("cpd@app.com");

        return updateCorporateProfileRequest;
    }

    private void seedData() {

        profilePersonal.setEmail("mikey@app.com");
        profilePersonal.setFirstName("Mike");
        profilePersonal.setSurname("Ang");
        profilePersonal.setPhoneNumber("0029934");
        profilePersonal.setUserId("123");
        profilePersonal.setDeleted(false);

        if (!profileRepository.existsByEmail(profilePersonal.getEmail()))
            profileRepository.save(profilePersonal);

        //personal profile 1
        profile.setGender("male");
        profile.setPhoneNumber("09123");
        profile.setEmail("app@app.com");
        profile.setFirstName("app");
        profile.setSurname("app");
        profile.setState("state");
        profile.setCorporate(false);
        profile.setUserId("1234");
        profile.setDeleted(false);

        if (!profileRepository.existsByEmail(profile.getEmail()))
            profileRepository.save(profile);

        ReferralCode referralCode = new ReferralCode();
        referralCode.setReferralCode("102kkdjeurw2");
        referralCode.setProfile(profile);
        referralCode.setUserId("1234");
        if(!referralCodeRepository.existsByEmail("102kkdjeurw2", "1234"))
            referralCodeRepository.save(referralCode);

        //corporate profile 1
        OtherDetails otherDetails = new OtherDetails();
        otherDetails.setOrganisationType("organisationType");
        otherDetails.setBusinessType("businessType");
        otherDetails.setOrganisationName("organisation name");

        Profile corporate = new Profile();
        corporate.setOrganisationName("name");
        corporate.setCity("city");
        corporate.setReferral("102kkdjeurw2");
        corporate.setUserId("42");
        corporate.setCorporate(true);
        corporate.setDeleted(false);
        corporate.setSurname("surname");
        corporate.setFirstName("first name");
        corporate.setEmail("cpd@app.com");
        corporate.setPhoneNumber("09123");
        corporate.setOtherDetails(otherDetails);

        if (!profileRepository.existsByEmail(corporate.getEmail()))
            profileRepository.save(corporate);


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

