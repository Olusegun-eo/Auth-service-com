package com.waya.wayaauthenticationservice.controller;

import static com.waya.wayaauthenticationservice.util.Constant.MESSAGE_400;
import static com.waya.wayaauthenticationservice.util.Constant.MESSAGE_422;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;

import com.waya.wayaauthenticationservice.enums.Type;
import com.waya.wayaauthenticationservice.enums.UploadType;
import com.waya.wayaauthenticationservice.pojo.others.CorporateProfileRequest;
import com.waya.wayaauthenticationservice.pojo.others.DeleteRequest;
import com.waya.wayaauthenticationservice.pojo.others.PersonalProfileRequest;
import com.waya.wayaauthenticationservice.pojo.others.ToggleSMSRequest;
import com.waya.wayaauthenticationservice.pojo.others.UpdateCorporateProfileRequest;
import com.waya.wayaauthenticationservice.pojo.others.UpdatePersonalProfileRequest;
import com.waya.wayaauthenticationservice.response.ApiResponse;
import com.waya.wayaauthenticationservice.response.DeleteResponse;
import com.waya.wayaauthenticationservice.response.ToggleSMSResponse;
import com.waya.wayaauthenticationservice.response.UserProfileResponse;
import com.waya.wayaauthenticationservice.service.ProfileService;
import com.waya.wayaauthenticationservice.util.CustomValidator;
import com.waya.wayaauthenticationservice.util.EnumValue;
import com.waya.wayaauthenticationservice.util.ValidPhone;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "PROFILE RESOURCE", description = "REST API for Profile Service API")
@CrossOrigin
@RestController
@RequestMapping("/api/v1/profile")
@Validated
public class ProfileController {

	private final ProfileService profileService;

	@Value("${api.server.deployed}")
	private String urlRedirect;

	@Autowired
	public ProfileController(ProfileService profileService) {
		this.profileService = profileService;
	}

	/**
	 * endpoint to get a users profile
	 *
	 * @param userId  user id
	 * @param request HttpServletRequest
	 * @return Object
	 */
	@ApiOperation(value = "${api.profile.get-user-profile.description}", notes = "${api.profile.get-user-profile.notes}", tags = {
			"PROFILE RESOURCE" })
	@ApiResponses(value = { @io.swagger.annotations.ApiResponse(code = 400, message = MESSAGE_400),
			@io.swagger.annotations.ApiResponse(code = 422, message = MESSAGE_422) })
	@GetMapping("/{userId}")
	ResponseEntity<ApiResponse<UserProfileResponse>> getUsersProfile(@PathVariable String userId,
			HttpServletRequest request) {
		UserProfileResponse profileResponse = profileService.getUserProfile(userId, request);
		return new ResponseEntity<>(new ApiResponse<>(profileResponse, "retrieved successfully", true), HttpStatus.OK);
	}

	/**
	 * endpoint to create personal profile
	 *
	 * @param personalProfileRequest personal profile request
	 * @return Object
	 */
	@ResponseStatus(HttpStatus.CREATED)
	@ApiOperation(value = "${api.profile.create-personal-profile.description}", notes = "${api.profile.create-personal-profile.notes}", tags = {
			"PROFILE RESOURCE" })
	@ApiResponses(value = { @io.swagger.annotations.ApiResponse(code = 400, message = MESSAGE_400),
			@io.swagger.annotations.ApiResponse(code = 422, message = MESSAGE_422) })
	@PostMapping("personal-profile")
	public ApiResponse<String> createPersonalProfile(final HttpServletRequest request,
			@Valid @RequestBody PersonalProfileRequest personalProfileRequest) {
		return profileService.createProfile(personalProfileRequest, getBaseUrl(request));
	}

	/**
	 * endpoint to create a corporate profile.
	 *
	 * @param corporateProfileRequest request
	 * @return Object
	 */
	@ResponseStatus(HttpStatus.CREATED)
	@ApiOperation(value = "${api.corporate-profile.create-corporate-profile.description}", notes = "${api.corporate-profile.create-corporate-profile.notes}", tags = {
			"PROFILE RESOURCE" })
	@ApiResponses(value = { @io.swagger.annotations.ApiResponse(code = 400, message = MESSAGE_400),
			@io.swagger.annotations.ApiResponse(code = 422, message = MESSAGE_422) })
	@PostMapping("corporate-profile")
	ApiResponse<String> createCorporateProfile(final HttpServletRequest request,
			@Valid @RequestBody CorporateProfileRequest corporateProfileRequest) {

		return profileService.createProfile(corporateProfileRequest, getBaseUrl(request));
	}

	/**
	 * endpoint to update personal profile.
	 *
	 * @param updatePersonalProfileRequest personal profile request
	 * @param userId                       user id
	 * @return Object
	 */
	@ApiOperation(value = "${api.profile.update-user-profile.description}", notes = "${api.profile.update-user-profile.notes}", tags = {
			"PROFILE RESOURCE" })
	@ApiResponses(value = { @io.swagger.annotations.ApiResponse(code = 400, message = MESSAGE_400),
			@io.swagger.annotations.ApiResponse(code = 422, message = MESSAGE_422) })
	@PutMapping("update-personal-profile/{userId}")
	ResponseEntity<ApiResponse<Object>> updateProfile(
			@Valid @RequestBody UpdatePersonalProfileRequest updatePersonalProfileRequest,
			@PathVariable @CustomValidator(message = "UserId must be numeric", type = Type.NUMERIC_STRING) String userId) {
		UserProfileResponse profileResponse = profileService.updateProfile(updatePersonalProfileRequest, userId);
		return new ResponseEntity<>(new ApiResponse<>(profileResponse, "profile updated successfully", true),
				HttpStatus.CREATED);
	}

	/**
	 * endpoint o update a corporate profile
	 *
	 * @param updateCorporateProfileRequest corporate profile request
	 * @param userId                        user id
	 * @return Object
	 */
	@ApiOperation(value = "${api.corporate-profile.update-corporate-profile.description}", notes = "${api.corporate-profile.update-corporate-profile.notes}", tags = {
			"PROFILE RESOURCE" })
	@ApiResponses(value = { @io.swagger.annotations.ApiResponse(code = 400, message = MESSAGE_400),
			@io.swagger.annotations.ApiResponse(code = 422, message = MESSAGE_422) })
	@PutMapping("update-corporate-profile/{userId}")
	ResponseEntity<ApiResponse<UserProfileResponse>> updateCorporateProfile(
			@Valid @RequestBody UpdateCorporateProfileRequest updateCorporateProfileRequest,
			@PathVariable @CustomValidator(message = "UserId must be numeric", type = Type.NUMERIC_STRING) String userId) {

		UserProfileResponse corporateProfileResponse = profileService.updateProfile(updateCorporateProfileRequest,
				userId);

		return new ResponseEntity<>(new ApiResponse<>(corporateProfileResponse, "profile updated successfully", true),
				HttpStatus.CREATED);
	}

	/**
	 * endpoint to update profile image
	 *
	 * @param file   file
	 * @param userId user id
	 * @return Object
	 * @throws MaxUploadSizeExceededException exception
	 */
	@ApiOperation(value = "${api.profile.update-user-profile-image.description}", notes = "${api.profile.update-user-profile-image.notes}", tags = {
			"PROFILE RESOURCE" })
	@ApiResponses(value = { @io.swagger.annotations.ApiResponse(code = 400, message = MESSAGE_400),
			@io.swagger.annotations.ApiResponse(code = 422, message = MESSAGE_422) })
	@PostMapping("/update-profile-image/{userId}")
	public ResponseEntity<ApiResponse<String>> updateProfileImage(@RequestPart MultipartFile file,
			@PathVariable @CustomValidator(message = "UserId must be numeric", type = Type.NUMERIC_STRING) String userId)
			throws MaxUploadSizeExceededException {
		return ResponseEntity.ok(profileService.updateProfileImage(userId, file));
	}

	@ApiOperation(value = "${api.profile.update-user-images.description}", notes = "${api.profile.update-user-images.notes}", tags = {
			"PROFILE RESOURCE" })
	@ApiResponses(value = { @io.swagger.annotations.ApiResponse(code = 400, message = MESSAGE_400),
			@io.swagger.annotations.ApiResponse(code = 422, message = MESSAGE_422) })
	@PostMapping("/update-profile-image/{type}/{userId}")
	public ResponseEntity<ApiResponse<String>> updateProfileImage(@RequestPart MultipartFile file,
			@PathVariable @CustomValidator(message = "UserId must be numeric", type = Type.NUMERIC_STRING) String userId,
			@PathVariable @EnumValue(enumClass = UploadType.class, message = "Must be either of type FRONT, LEFT or RIGHT") String type)
			throws MaxUploadSizeExceededException {
		return ResponseEntity.ok(profileService.uploadOtherImage(userId, file, type));
	}

	/**
	 * get all users referrals referals
	 *
	 * @param page   page
	 * @param userId user id
	 * @return List<UserProfileResponse>
	 */
	@ApiOperation(value = "${api.profile.referals.description}", notes = "${api.profile.referals.notes}", tags = {
			"PROFILE RESOURCE" })
	@ApiResponses(value = { @io.swagger.annotations.ApiResponse(code = 400, message = MESSAGE_400),
			@io.swagger.annotations.ApiResponse(code = 422, message = MESSAGE_422) })
	@ResponseStatus(HttpStatus.OK)
	@GetMapping("user-referrals/{userId}")
	ResponseEntity<?> getAllUsersReferrals(@RequestParam(required = false, defaultValue = "0") String page,
			@PathVariable @CustomValidator(message = "UserId must be numeric", type = Type.NUMERIC_STRING) String userId) {
		return new ResponseEntity<>(profileService.findAllUserReferral(userId, page), HttpStatus.OK);
	}

	@ApiOperation(value = "Delete user", notes = "Toggle delete for user profile", tags = { "PROFILE RESOURCE" })
	@ApiResponses(value = { @io.swagger.annotations.ApiResponse(code = 400, message = MESSAGE_400),
			@io.swagger.annotations.ApiResponse(code = 422, message = MESSAGE_422) })
	@PutMapping("delete-restore")
	ResponseEntity<DeleteResponse> toggleDelete(@Valid @RequestBody DeleteRequest deleteRequest) {
		return new ResponseEntity<>(profileService.toggleDelete(deleteRequest), HttpStatus.OK);
	}

	@ApiOperation(value = "SMS Alert", notes = "SMS Alert: user can enable or disable sms alert", tags = {
			"PROFILE RESOURCE" })
	@ApiResponses(value = { @io.swagger.annotations.ApiResponse(code = 400, message = MESSAGE_400),
			@io.swagger.annotations.ApiResponse(code = 422, message = MESSAGE_422) })
	@PostMapping("/toggle-sms-alerts")
	ResponseEntity<ApiResponse<ToggleSMSResponse>> toggleSMSAlert(
			@Valid @RequestBody ToggleSMSRequest toggleSMSRequest) {

		ToggleSMSResponse toggleSMSResponse = profileService.toggleSMSAlert(toggleSMSRequest);
		ApiResponse<ToggleSMSResponse> response = new ApiResponse<>(toggleSMSResponse, "Data retrieved successfully",
				true);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@ApiOperation(value = "Check SMS Alert Status", tags = {
			"PROFILE RESOURCE" }, notes = "Config SMS Charge Fee: User can check status of sms alert")
	@ApiResponses(value = { @io.swagger.annotations.ApiResponse(code = 400, message = MESSAGE_400),
			@io.swagger.annotations.ApiResponse(code = 422, message = MESSAGE_422) })
	@ResponseStatus(HttpStatus.OK)
	@GetMapping("/sms-alert/status/{phoneNumber}")
	ResponseEntity<ApiResponse<ToggleSMSResponse>> getSMSAlertStatus(
			@Valid @ApiParam(example = "2348054354344") @PathVariable @ValidPhone String phoneNumber) {

		ToggleSMSResponse toggleSMSResponse = profileService.getSMSAlertStatus(phoneNumber);
		System.out.println(" ### back from service class smsCharges: :::: " + toggleSMSResponse);
		ApiResponse<ToggleSMSResponse> response = new ApiResponse<>(toggleSMSResponse, "Data created successfully",
				true);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@ApiOperation(value = "Get Profile By Referral Code", tags = {
			"PROFILE RESOURCE" }, notes = "View Active SMS Charge: Admin can validate that the referral code belongs to another user")
	@ApiResponses(value = { @io.swagger.annotations.ApiResponse(code = 400, message = MESSAGE_400),
			@io.swagger.annotations.ApiResponse(code = 422, message = MESSAGE_422) })
	@ResponseStatus(HttpStatus.OK)
	@GetMapping("/get-user-profile/{referralCode}")
	ResponseEntity<ApiResponse<UserProfileResponse>> getProfileByReferralCode(@PathVariable String referralCode) {
		UserProfileResponse userProfileResponse = profileService.getProfileByReferralCode(referralCode);
		ApiResponse<UserProfileResponse> response = new ApiResponse<>(userProfileResponse, "Data created successfully",
				true);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	private String getBaseUrl(HttpServletRequest request) {
		return "http://" + urlRedirect + ":" + request.getServerPort() + request.getContextPath();
	}

}
