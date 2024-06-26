package com.waya.wayaauthenticationservice.service;

import com.waya.wayaauthenticationservice.entity.Profile;
import com.waya.wayaauthenticationservice.entity.ReferralBonusEarning;
import com.waya.wayaauthenticationservice.entity.SMSAlertConfig;
import com.waya.wayaauthenticationservice.entity.Users;
import com.waya.wayaauthenticationservice.pojo.others.*;
import com.waya.wayaauthenticationservice.response.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface ProfileService {

	/**
	 * create a new personal Profile.
	 *
	 * @param personalProfileRequest personal profile request
	 */
	ApiResponseBody<String> createProfile(final PersonalProfileRequest personalProfileRequest, String baseUrl);

	/**
	 * create a new corporate profile.
	 *
	 * @param profileRequest corporate profile request
	 */
	ApiResponseBody<String> createProfile(final CorporateProfileRequest profileRequest, String baseUrl);

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
	UserProfileResponse updateProfile(UpdatePersonalProfileRequest updatePersonalProfileRequest, final String userId);

	/**
	 * update a corporate profile
	 *
	 * @param corporateProfileRequest corporate profile request
	 * @param userId                  user id
	 * @return CorporateProfileResponse
	 */
	UserProfileResponse updateProfile(final UpdateCorporateProfileRequest corporateProfileRequest, final String userId);

	/**
	 * @param userId              user id
	 * @param profileImageRequest request
	 */
	ApiResponseBody<String> updateProfileImage(final Long userId, MultipartFile profileImageRequest);

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
	DeleteResponse toggleDelete(DeleteRequest deleteRequest);

	SMSResponse toggleSMSAlert(SMSRequest SMSRequest);
	List<SMSAlertConfig> getPhoneNumber(String phoneNumber);

	SMSResponse getSMSAlertStatus(String phoneNumber);

	void sendWelcomeEmail(Users user);

	UserProfileResponse getProfileByReferralCode(String referralCode);

	UserProfileResponse saveDeviceToken(DeviceTokenRequest deviceTokenRequest) throws Exception;

	Profile getProfile(String userId);

	void postMigrateReferralCode(UUID profileId, String userId, String token) throws Exception;


	/**
	 * 
	 * @param userId: Corporate UserId
	 * @param file: Multipart Image file to upload
	 * @param type: either FRONT, LEFT or RIGHT
	 * @return
	 */
	ApiResponseBody<String> uploadOtherImage(Long userId, MultipartFile file, String type);


	List<WalletTransactionPojo> sendSignUpBonusToUser(String userId);


	ResponseEntity<ReferralCodePojo> checkReferralCode2(String userId) throws Exception;
}
