package com.waya.wayaauthenticationservice.service;

import com.waya.wayaauthenticationservice.pojo.*;
import com.waya.wayaauthenticationservice.response.*;
import com.waya.wayaauthenticationservice.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface ProfileService {

    /**
     * create a new personal Profile.
     *
     * @param personalProfileRequest personal profile request
     */
    ApiResponse<String> createProfile(final PersonalProfileRequest personalProfileRequest);

    /**
     * create a new corporate profile.
     *
     * @param profileRequest corporate profile request
     */
    ApiResponse<String> createProfile(final CorporateProfileRequest profileRequest);

    /**
     * gets a users profile
     *
     * @param userId user id
     * @return PersonalProfileResponse
     */
    UserProfileResponse getUserProfile(final String userId, HttpServletRequest httpRequest);

    /**
     * updates a users personal profile
     *
     * @param updatePersonalProfileRequest profile update request
     * @param userId                       user id
     */
    UserProfileResponse updateProfile(UpdatePersonalProfileRequest updatePersonalProfileRequest,
                                      final String userId);

    /**
     * update a corporate profile
     *
     * @param corporateProfileRequest corporate profile request
     * @param userId                  user id
     * @return CorporateProfileResponse
     */
    UserProfileResponse updateProfile(final UpdateCorporateProfileRequest corporateProfileRequest,
                                      final String userId);

    /**
     * @param userId              user id
     * @param profileImageRequest request
     */
    CompletableFuture<ApiResponse<ProfileImageResponse>> updateProfileImage(final String userId,
                                                                            MultipartFile profileImageRequest);

    /**
     * search for profile by name
     *
     * @param name fullName
     * @return List<ProfilePersonal>
     */
    List<SearchProfileResponse> searchProfileByName(final String name);

    /**
     * search for profile by phone number
     *
     * @param phoneNumber phone number
     * @return List<SearchProfileResponse></SearchProfileResponse>
     */
    List<SearchProfileResponse> searchProfileByPhoneNumber(final String phoneNumber);

    /**
     * search for profile by email
     *
     * @param email email
     * @return List<SearchProfileResponse>
     */
    List<SearchProfileResponse> searchProfileByEmail(final String email);

    /**
     * search profile by organization name
     *
     * @param name name
     * @return List<SearchProfileResponse>
     */
    List<SearchProfileResponse> searchProfileByOrganizationName(final String name);

    /**
     * @param userId userid
     * @return List<UserProfileResponse>
     */
    List<UserProfileResponse> findAllUserReferral(String userId, String page);

    /**
     *
     * @param deleteRequest deleteRequest
     * @return DeleteResponse
     */
    ResponseEntity<DeleteResponse> toggleDelete(DeleteRequest deleteRequest);


    ToggleSMSResponse toggleSMSAlert(ToggleSMSRequest toggleSMSRequest);

    SMSChargeResponse configureSMSCharge(SMSChargeFeeRequest smsChargeFeeRequest);

    ToggleSMSResponse getSMSAlertStatus(String phoneNumber);

    SMSChargeResponse toggleSMSCharge(Long id);

    SMSChargeResponse getActiveSMSCharge();


    void sendWelcomeEmail(String email);

    UserProfileResponse getProfileByReferralCode(String referralCode);
}
