package com.waya.wayaauthenticationservice.integration;

import com.waya.wayaauthenticationservice.entity.OtherDetails;
import com.waya.wayaauthenticationservice.entity.Profile;
import com.waya.wayaauthenticationservice.enums.DeleteType;
import com.waya.wayaauthenticationservice.pojo.*;
import com.waya.wayaauthenticationservice.repository.OtherDetailsRepository;
import com.waya.wayaauthenticationservice.repository.ProfileRepository;
import com.waya.wayaauthenticationservice.response.ProfileImageResponse;
import com.waya.wayaauthenticationservice.service.FileResourceServiceFeignClient;
import static com.waya.wayaauthenticationservice.util.JsonString.asJsonString;

import com.waya.wayaauthenticationservice.util.Constant;
import com.waya.wayaauthenticationservice.util.profile.ApiResponse;
import org.hamcrest.core.Is;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
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

import java.nio.charset.StandardCharsets;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest(properties = {"eureka.client.enabled=false"})
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DirtiesContext
@EmbeddedKafka(partitions = 1, brokerProperties =
        {"listeners=PLAINTEXT://localhost:9092", "port=9092"})
class ProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private OtherDetailsRepository otherDetailsRepository;


    @MockBean
    private FileResourceServiceFeignClient fileResourceServiceFeignClient;

    private final Profile profilePersonal = new Profile();
    private final Profile profile = new Profile();

    final MockMultipartFile file = new MockMultipartFile("files",
            "snapshot.png", MediaType.IMAGE_JPEG_VALUE, "content".getBytes(StandardCharsets.UTF_8));

    private final ProfileImageResponse profileImageResponse =
            new ProfileImageResponse("image url");

    private final ApiResponse<ProfileImageResponse> apiResponse =
            new ApiResponse<>(profileImageResponse, "success", true);

    final String setUpUserId = "uew748";

    @BeforeAll
    public void setUp() {
        seedData();
    }

    @Test
    @DisplayName("create personal profile successfully 💯")
    void createPersonalProfile() throws Exception {
        final PersonalProfileRequest personalProfileRequest = setPersonalProfileData(
                "kola@app.com", "244");

        createAndVerifyPersonalProfile(personalProfileRequest, "$.message",
                "profile created. An OTP has been sent to your phone",
                "$.httpStatus", "OK");
    }

    @Test
    @DisplayName("create personal profile when email already exist 🔥")
    void createPersonalProfileWhenProfileWithEmailExist() throws Exception {
        final PersonalProfileRequest personalProfileRequest = setPersonalProfileData(
                "app@app.com", "2ewe");

        createAndVerifyPersonalProfile(personalProfileRequest, "$.message",
                Constant.DUPLICATE_KEY,
                "$.httpStatus", "UNPROCESSABLE_ENTITY");
    }

    @Test
    @DisplayName("create personal profile when userId already exist 🔥")
    void createPersonalProfileWhenUserIdExist() throws Exception {
        final PersonalProfileRequest personalProfileRequest = setPersonalProfileData(
                "app@app12.com", "123");

        createAndVerifyPersonalProfile(personalProfileRequest, "$.message",
                "user id already exists",
                "$.httpStatus", "UNPROCESSABLE_ENTITY");
    }


    @Test
    @DisplayName("create corporate profile successfully 💯")
    void createCorporateProfile() throws Exception {
        final CorporateProfileRequest corporateProfileRequest = setCorporateProfileData(
                "rt@app.com", "5432");

        createAndVerfyCorporateProfile(corporateProfileRequest, "$.message",
                "profile created. An OTP has been sent to your phone",
                "$.httpStatus", "OK");
    }

    @Test
    @DisplayName("create corporate profile when email exist 💯")
    void createCorporateProfileWhenEmailExist() throws Exception {
        final CorporateProfileRequest corporateProfileRequest = setCorporateProfileData(
                "app@app.com", "5432");

        createAndVerfyCorporateProfile(corporateProfileRequest, "$.message",
                Constant.DUPLICATE_KEY,
                "$.httpStatus", "UNPROCESSABLE_ENTITY");
    }

    @Test
    @DisplayName("create corporate profile when user id exist 💯")
    void createCorporateProfileWhenUserIdExist() throws Exception {
        final CorporateProfileRequest corporateProfileRequest = setCorporateProfileData(
                "app@app.com", "123");

        createAndVerfyCorporateProfile(corporateProfileRequest, "$.message",
                Constant.DUPLICATE_KEY,
                "$.httpStatus", "UNPROCESSABLE_ENTITY");
    }

    @Test
    @DisplayName("get a users profile")
    void getPersonalProfile() throws Exception {
        getAndVerifyUserProfile("123", "$.message",
                "retrieved successfully", status().isOk());
    }

    @Test
    @DisplayName("get a corporate users profile")
    void getCorporateProfile() throws Exception {
        getAndVerifyUserProfile("42", "$.message",
                "retrieved successfully", status().isOk());
    }

    @Test
    @DisplayName("get a users profile when profile does not exist")
    void getUsersProfileWhenProfileNotExist() throws Exception {
        getAndVerifyUserProfile("2iu24", "$.message",
                "profile with that user id is not found", status().isBadRequest());
    }

    @Test
    @DisplayName("update a personal profile successfully")
    void updatePersonalProfile() throws Exception {

        final UpdatePersonalProfileRequest updatePersonalProfileRequest = setUpdatePersonalProfileRequest();

        updateAndVerifyPersonalProfile(updatePersonalProfileRequest, "123", "$.message",
                "profile updated successfully", status().isCreated());
    }

    @Test
    @DisplayName("update a personal profile when user id is invalid")
    void updatePersonalProfileWithInValidUserId() throws Exception {

        final UpdatePersonalProfileRequest updatePersonalProfileRequest = setUpdatePersonalProfileRequest();

        updateAndVerifyPersonalProfile(updatePersonalProfileRequest, "121233", "$.message",
                Constant.PROFILE_NOT_EXIST, status().isBadRequest());
    }

    @Test
    @DisplayName("update a corporate profile successfully")
    void updateCorporateProfile() throws Exception {
        final UpdateCorporateProfileRequest updateCorporateProfileRequest = setUpdateCorporateProfileRequest();

        updateAndVerifyCorporateProfile(updateCorporateProfileRequest, "42", "$.message",
                "profile updated successfully", status().isCreated());
    }

    @Test
    @DisplayName("update a corporate profile when user id is not found")
    void updateCorporateProfileWithInvalidUserId() throws Exception {
        final UpdateCorporateProfileRequest updateCorporateProfileRequest = setUpdateCorporateProfileRequest();

        updateAndVerifyCorporateProfile(updateCorporateProfileRequest, "4242", "$.message",
                "user with that id not found", status().isUnprocessableEntity());
    }

    @Test
    @DisplayName("get all users referrals successfully")
    void getAllUsersReferral() throws Exception {
        getAndVerifyAllUsersReferrals(profile.getUserId(), status().isOk());
    }

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

    @Test
    @DisplayName("delete personal profile error 💯")
    void deleteProfileError() throws Exception {



        DeleteRequest deleteRequest = DeleteRequest.builder()
                .deleteType(DeleteType.DELETE).userId("10").build();

        deleteProfile(deleteRequest, "$.error",
                "Profile with userId do not exist or already deleted",
                "$.code", "300");
    }

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

        mockMvc.perform(post("/personal-profile")
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

        mockMvc.perform(put("/delete-restore")
                .contentType(APPLICATION_JSON)
                .content(asJsonString(deleteRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(jsonPath0, Is.is(jsonPathMessage0)))
                .andExpect(jsonPath(jsonPath1, Is.is(jsonPathMessage1)));
    }

    private void createAndVerfyCorporateProfile(
            final CorporateProfileRequest corporateProfileRequest,
            final String jsonPath0, final String jsonPathMessage0,
            final String jsonPath1, final String jsonPathMessage1
    ) throws Exception {
        mockMvc.perform(post("/corporate-profile")
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
        mockMvc.perform(get("/profile/" + userId)
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

        mockMvc.perform(put("/update-personal-profile/" + userId)
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
        mockMvc.perform(put("/update-corporate-profile/" + userId)
                .contentType(APPLICATION_JSON)
                .content(asJsonString(corporateProfileRequest)))
                .andExpect(expectedStatus)
                .andExpect(jsonPath(jsonPath, Is.is(jsonPathMessage)));
    }

    private void getAndVerifyAllUsersReferrals(
            final String userId, ResultMatcher expectedStatus
    ) throws Exception {
        mockMvc.perform(get("/user-referrals/" + userId)
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

        profilePersonal.setEmail("mike@app.com");
        profilePersonal.setFirstName("Mike");
        profilePersonal.setSurname("Ang");
        profilePersonal.setPhoneNumber("0029934");
        profilePersonal.setUserId(setUpUserId);
        profilePersonal.setDeleted(false);

        profileRepository.save(profilePersonal);
        //personal profile 1
        profile.setGender("male");
        profile.setPhoneNumber("09123");
        profile.setEmail("app@app.com");
        profile.setFirstName("app");
        profile.setSurname("app");
        profile.setState("state");
        profile.setCorporate(false);
        profile.setUserId("123");
        profile.setDeleted(false);

        profileRepository.save(profile);

//        ReferralCode referralCode = new ReferralCode();
//        referralCode.setReferalCode("102kkdjeurw2");
//        referralCode.setProfile(profile);
//        referralCode.setUserId("123");
//
//        referralCodeRepository.save(referralCode);

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

        profileRepository.save(corporate);

    }

}

