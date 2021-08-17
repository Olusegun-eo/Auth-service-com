package com.waya.wayaauthenticationservice.controller;

import com.waya.wayaauthenticationservice.enums.Type;
import com.waya.wayaauthenticationservice.enums.UploadType;
import com.waya.wayaauthenticationservice.pojo.others.*;
import com.waya.wayaauthenticationservice.response.ApiResponseBody;
import com.waya.wayaauthenticationservice.response.DeleteResponse;
import com.waya.wayaauthenticationservice.response.SMSResponse;
import com.waya.wayaauthenticationservice.response.UserProfileResponse;
import com.waya.wayaauthenticationservice.service.ProfileService;
import com.waya.wayaauthenticationservice.util.CustomValidator;
import com.waya.wayaauthenticationservice.util.EnumValue;
import com.waya.wayaauthenticationservice.util.ValidPhone;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import static com.waya.wayaauthenticationservice.util.Constant.MESSAGE_400;
import static com.waya.wayaauthenticationservice.util.Constant.MESSAGE_422;

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
	ResponseEntity<ApiResponseBody<UserProfileResponse>> getUsersProfile(@PathVariable String userId,
			HttpServletRequest request) {
		UserProfileResponse profileResponse = profileService.getUserProfile(userId, request);
		return new ResponseEntity<>(new ApiResponseBody<>(profileResponse, "retrieved successfully", true), HttpStatus.OK);
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
	public ApiResponseBody<String> createPersonalProfile(final HttpServletRequest request,
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
	@ApiOperation(value = "${api.corporate-profile.create-corporate-profile.description}",
			notes = "${api.corporate-profile.create-corporate-profile.notes}", tags = {
			"PROFILE RESOURCE" })
	@ApiResponses(value = { @ApiResponse(code = 400, message = MESSAGE_400),
			@ApiResponse(code = 422, message = MESSAGE_422) })
	@PostMapping("corporate-profile")
	ApiResponseBody<String> createCorporateProfile(final HttpServletRequest request,
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
	@ApiResponses(value = { @ApiResponse(code = 400, message = MESSAGE_400),
			@ApiResponse(code = 422, message = MESSAGE_422) })
	@PutMapping("update-personal-profile/{userId}")
	@PreAuthorize(value = "@userSecurity.useHierarchy(#userId, authentication)")
	ResponseEntity<ApiResponseBody<Object>> updateProfile(
			@Valid @RequestBody UpdatePersonalProfileRequest updatePersonalProfileRequest,
			@PathVariable @NotNull(message = "UserId cannot be Null") Long userId) {
		UserProfileResponse profileResponse = profileService.updateProfile(updatePersonalProfileRequest, String.valueOf(userId));
		return new ResponseEntity<>(new ApiResponseBody<>(profileResponse, "profile updated successfully", true),
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
	@PreAuthorize(value = "@userSecurity.useHierarchy(#userId, authentication)")
	ResponseEntity<ApiResponseBody<UserProfileResponse>> updateCorporateProfile(
			@Valid @RequestBody UpdateCorporateProfileRequest updateCorporateProfileRequest,
			@PathVariable @NotNull(message = "UserId cannot be Null") Long userId) {
		UserProfileResponse corporateProfileResponse = profileService.updateProfile(updateCorporateProfileRequest,
				String.valueOf(userId));
		return new ResponseEntity<>(new ApiResponseBody<>(corporateProfileResponse, "profile updated successfully", true),
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
	@PreAuthorize(value = "@userSecurity.useHierarchy(#userId, authentication)")
	public ResponseEntity<ApiResponseBody<String>> updateProfileImage(@RequestPart MultipartFile file,
																	  @PathVariable @NotNull(message = "UserId cannot be Null") Long userId)
			throws MaxUploadSizeExceededException {
		return ResponseEntity.ok(profileService.updateProfileImage(userId, file));
	}

	@ApiOperation(value = "${api.profile.update-user-images.description}", notes = "${api.profile.update-user-images.notes}", tags = {
			"PROFILE RESOURCE" })
	@ApiResponses(value = { @io.swagger.annotations.ApiResponse(code = 400, message = MESSAGE_400),
			@io.swagger.annotations.ApiResponse(code = 422, message = MESSAGE_422) })
	@PostMapping("/update-profile-image/{type}/{userId}")
	@PreAuthorize(value = "@userSecurity.useHierarchy(#userId, authentication)")
	public ResponseEntity<ApiResponseBody<String>> updateProfileImage(@RequestPart MultipartFile file,
			@PathVariable @NotNull(message = "UserId cannot be Null") Long userId,
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
	@PreAuthorize(value = "@userSecurity.useHierarchy(#request.userId, authentication)")
	ResponseEntity<DeleteResponse> toggleDelete(@Valid @RequestBody DeleteRequest request) {
		return new ResponseEntity<>(profileService.toggleDelete(request), HttpStatus.OK);
	}

	@ApiOperation(value = "SMS Alert", notes = "SMS Alert: user can enable or disable sms alert", tags = {
			"PROFILE RESOURCE" })
	@ApiResponses(value = { @io.swagger.annotations.ApiResponse(code = 400, message = MESSAGE_400),
			@io.swagger.annotations.ApiResponse(code = 422, message = MESSAGE_422) })
	@PostMapping("/sms-alert")
	@PreAuthorize(value = "@userSecurity.useHierarchy(#request.phoneNumber, authentication)")
	ResponseEntity<ApiResponseBody<SMSResponse>> toggleSMSAlert(
			@Valid @RequestBody SMSRequest request) {
		SMSResponse SMSResponse = profileService.toggleSMSAlert(request);
		ApiResponseBody<SMSResponse> response = new ApiResponseBody<>(SMSResponse, "Data retrieved successfully",
				true);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@ApiOperation(value = "Check SMS Alert Status", tags = {
			"PROFILE RESOURCE" }, notes = "Config SMS Charge Fee: User can check status of sms alert")
	@ApiResponses(value = { @io.swagger.annotations.ApiResponse(code = 400, message = MESSAGE_400),
			@io.swagger.annotations.ApiResponse(code = 422, message = MESSAGE_422) })
	@PreAuthorize(value = "@userSecurity.useHierarchy(#phoneNumber, authentication)")
	@GetMapping("/sms-alert/status/{phoneNumber}")
	ResponseEntity<ApiResponseBody<SMSResponse>> getSMSAlertStatus(
			@Valid @ApiParam(example = "2348054354344") @PathVariable @ValidPhone String phoneNumber) {

		SMSResponse SMSResponse = profileService.getSMSAlertStatus(phoneNumber);
		System.out.println("Back from service class smsCharges: :::: " + SMSResponse);
		ApiResponseBody<SMSResponse> response = new ApiResponseBody<>(SMSResponse, "Data created successfully",
				true);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@ApiOperation(value = "Get Profile By Referral Code", tags = {
			"PROFILE RESOURCE" }, notes = "View Active SMS Charge: Admin can validate that the referral code belongs to another user")
	@ApiResponses(value = { @io.swagger.annotations.ApiResponse(code = 400, message = MESSAGE_400),
			@io.swagger.annotations.ApiResponse(code = 422, message = MESSAGE_422) })
	@ResponseStatus(HttpStatus.OK)
	@GetMapping("/get-user-profile/{referralCode}")
	ResponseEntity<ApiResponseBody<UserProfileResponse>> getProfileByReferralCode(@PathVariable String referralCode) {
		UserProfileResponse userProfileResponse = profileService.getProfileByReferralCode(referralCode);
		ApiResponseBody<UserProfileResponse> response = new ApiResponseBody<>(userProfileResponse, "Data created successfully",
				true);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	private String getBaseUrl(HttpServletRequest request) {
		return "http://" + urlRedirect + ":" + request.getServerPort() + request.getContextPath();
	}

}
