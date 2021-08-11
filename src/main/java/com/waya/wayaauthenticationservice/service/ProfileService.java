package com.waya.wayaauthenticationservice.service;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.multipart.MultipartFile;

import com.waya.wayaauthenticationservice.pojo.others.CorporateProfileRequest;
import com.waya.wayaauthenticationservice.pojo.others.DeleteRequest;
import com.waya.wayaauthenticationservice.pojo.others.PersonalProfileRequest;
import com.waya.wayaauthenticationservice.pojo.others.ToggleSMSRequest;
import com.waya.wayaauthenticationservice.pojo.others.UpdateCorporateProfileRequest;
import com.waya.wayaauthenticationservice.pojo.others.UpdatePersonalProfileRequest;
import com.waya.wayaauthenticationservice.response.ApiResponse;
import com.waya.wayaauthenticationservice.response.DeleteResponse;
import com.waya.wayaauthenticationservice.response.SearchProfileResponse;
import com.waya.wayaauthenticationservice.response.ToggleSMSResponse;
import com.waya.wayaauthenticationservice.response.UserProfileResponse;

public interface ProfileService {

	/**
	 * create a new personal Profile.
	 *
	 * @param personalProfileRequest personal profile request
	 */
	ApiResponse<String> createProfile(final PersonalProfileRequest personalProfileRequest, String baseUrl);

	/**
	 * create a new corporate profile.
	 *
	 * @param profileRequest corporate profile request
	 */
	ApiResponse<String> createProfile(final CorporateProfileRequest profileRequest, String baseUrl);

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
	ApiResponse<String> updateProfileImage(final String userId, MultipartFile profileImageRequest);

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

	ToggleSMSResponse toggleSMSAlert(ToggleSMSRequest toggleSMSRequest);

	ToggleSMSResponse getSMSAlertStatus(String phoneNumber);

	void sendWelcomeEmail(String email);

	UserProfileResponse getProfileByReferralCode(String referralCode);

	/**
	 * 
	 * @param userId: Corporate UserId
	 * @param file: Multipart Image file to upload
	 * @param type: either FRONT, LEFT or RIGHT
	 * @return
	 */
	ApiResponse<String> uploadOtherImage(String userId, MultipartFile file, String type);
}
