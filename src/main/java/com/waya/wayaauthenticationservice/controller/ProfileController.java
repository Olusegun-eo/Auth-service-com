package com.waya.wayaauthenticationservice.controller;

import com.waya.wayaauthenticationservice.pojo.others.*;
import com.waya.wayaauthenticationservice.response.*;
import com.waya.wayaauthenticationservice.service.ProfileService;
import com.waya.wayaauthenticationservice.response.ApiResponse;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.waya.wayaauthenticationservice.util.Constant.MESSAGE_400;
import static com.waya.wayaauthenticationservice.util.Constant.MESSAGE_422;

@Api(tags = {"Profile Resource"})
@SwaggerDefinition(tags = {
        @Tag(name = "Profile Resource", description = "REST API for Profile Service.")
})
@CrossOrigin
@RestController
@RequestMapping("/api/v1/profile")
public class ProfileController {

    private final ProfileService profileService;

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
    @ApiOperation(
            value = "${api.profile.get-user-profile.description}",
            notes = "${api.profile.get-user-profile.notes}")
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 400, message = MESSAGE_400),
            @io.swagger.annotations.ApiResponse(code = 422, message = MESSAGE_422)
    })
    @GetMapping("/{userId}")
    ResponseEntity<ApiResponse<UserProfileResponse>> getUsersProfile(@PathVariable String userId,
                                                                     HttpServletRequest request) {
        UserProfileResponse profileResponse = profileService.getUserProfile(userId, request);
        return new ResponseEntity<>(new ApiResponse<>(profileResponse,
                "retrieved successfully", true), HttpStatus.OK);
    }

    /**
     * endpoint to create personal profile
     *
     * @param personalProfileRequest personal profile request
     * @return Object
     */
    @ResponseStatus(HttpStatus.CREATED)
    @ApiOperation(
            value = "${api.profile.create-personal-profile.description}",
            notes = "${api.profile.create-personal-profile.notes}")
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 400, message = MESSAGE_400),
            @io.swagger.annotations.ApiResponse(code = 422, message = MESSAGE_422)
    })
    @PostMapping("personal-profile")
    public ApiResponse<String> createPersonalProfile(final HttpServletRequest request,
            @Valid @RequestBody PersonalProfileRequest personalProfileRequest
    ) {
        return profileService.createProfile(personalProfileRequest, getBaseUrl(request));
    }
    /**
     * endpoint to create a corporate profile.
     *
     * @param corporateProfileRequest request
     * @return Object
     */
    @ResponseStatus(HttpStatus.CREATED)
    @ApiOperation(
            value = "${api.corporate-profile.create-corporate-profile.description}",
            notes = "${api.corporate-profile.create-corporate-profile.notes}")
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 400, message = MESSAGE_400),
            @io.swagger.annotations.ApiResponse(code = 422, message = MESSAGE_422)
    })
    @PostMapping("corporate-profile")
    ApiResponse<String> createCorporateProfile(final HttpServletRequest request,
            @Valid @RequestBody CorporateProfileRequest corporateProfileRequest){

        return profileService.createProfile(corporateProfileRequest, getBaseUrl(request));
    }


    /**
     * endpoint to update personal profile.
     *
     * @param updatePersonalProfileRequest personal profile request
     * @param userId                       user id
     * @return Object
     */
    @ApiOperation(
            value = "${api.profile.update-user-profile.description}",
            notes = "${api.profile.update-user-profile.notes}")
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 400, message = MESSAGE_400),
            @io.swagger.annotations.ApiResponse(code = 422, message = MESSAGE_422)
    })
    @PutMapping("update-personal-profile/{userId}")
    ResponseEntity<ApiResponse<Object>> updateProfile(
            @Valid @RequestBody UpdatePersonalProfileRequest updatePersonalProfileRequest,
            @PathVariable String userId){
        UserProfileResponse profileResponse =
                profileService.updateProfile(updatePersonalProfileRequest, userId);
        return new ResponseEntity<>(new ApiResponse<>(profileResponse,
                "profile updated successfully", true), HttpStatus.CREATED);
    }

    /**
     * endpoint o update a corporate profile
     *
     * @param updateCorporateProfileRequest corporate profile request
     * @param userId                        user id
     * @return Object
     */
    @ApiOperation(
            value = "${api.corporate-profile.update-corporate-profile.description}",
            notes = "${api.corporate-profile.update-corporate-profile.notes}")
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 400, message = MESSAGE_400),
            @io.swagger.annotations.ApiResponse(code = 422, message = MESSAGE_422)
    })
    @PutMapping("update-corporate-profile/{userId}")
    ResponseEntity<ApiResponse<UserProfileResponse>> updateCorporateProfile(
            @Valid @RequestBody UpdateCorporateProfileRequest updateCorporateProfileRequest,
            @PathVariable String userId){

        UserProfileResponse corporateProfileResponse =
                profileService.updateProfile(updateCorporateProfileRequest, userId);

        return new ResponseEntity<>(new ApiResponse<>(corporateProfileResponse,
                "profile updated successfully", true), HttpStatus.CREATED);
    }


    /**
     * endpoint to update profile image
     *
     * @param file   file
     * @param userId user id
     * @return Object
     * @throws MaxUploadSizeExceededException exception
     */
    @ResponseStatus(HttpStatus.CREATED)
    @ApiOperation(
            value = "${api.profile.update-user-profile-image.description}",
            notes = "${api.profile.update-user-profile-image.notes}")
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 400, message = MESSAGE_400),
            @io.swagger.annotations.ApiResponse(code = 422, message = MESSAGE_422)
    })
    @PutMapping(value = "update-profile-image/{userId}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    CompletableFuture<ApiResponse<ProfileImageResponse>> updateProfileImage(
            @RequestPart MultipartFile file, @PathVariable String userId) throws MaxUploadSizeExceededException
  {

        return profileService.updateProfileImage(userId, file);
    }
    /**
     * get all users referrals   referals
     *
     * @param page   page
     * @param userId user id
     * @return List<UserProfileResponse>
     */
    @ApiOperation(
            value = "${api.profile.referals.description}",
            notes = "${api.profile.referals.notes}")
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 400, message = MESSAGE_400),
            @io.swagger.annotations.ApiResponse(code = 422, message = MESSAGE_422)
    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("user-referrals/{userId}")
    List<UserProfileResponse> getAllUsersReferrals(
            @RequestParam(required = false, defaultValue = "0") String page, @PathVariable String userId) {
        return profileService.findAllUserReferral(userId, page);
    }


    @ApiOperation(
            value = "Delete user",
            notes = "Toggle delete for user profile")
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 400, message = MESSAGE_400),
            @io.swagger.annotations.ApiResponse(code = 422, message = MESSAGE_422)
    })
    @PutMapping("delete-restore")
    ResponseEntity<DeleteResponse> toggleDelete(@Valid @RequestBody DeleteRequest deleteRequest){

        return profileService.toggleDelete(deleteRequest);
    }

    @ApiOperation(
            value = "SMS Alert",
            notes = "SMS Alert: user can enable or disable sms alert")
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 400, message = MESSAGE_400),
            @io.swagger.annotations.ApiResponse(code = 422, message = MESSAGE_422)
    })
    @PostMapping("/toggle-sms-alerts")
    ResponseEntity<ApiResponse<ToggleSMSResponse>> toggleSMSAlert(@Valid @RequestBody ToggleSMSRequest toggleSMSRequest){

        ToggleSMSResponse toggleSMSResponse = profileService.toggleSMSAlert(toggleSMSRequest);
        ApiResponse<ToggleSMSResponse> response = new ApiResponse<>(toggleSMSResponse, "Data retrieved successfully", true);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @ApiOperation(
            value = "Config SMS Charge Fee",
            notes = "Config SMS Charge Fee: Admin can create sms alert charge")
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 400, message = MESSAGE_400),
            @io.swagger.annotations.ApiResponse(code = 422, message = MESSAGE_422)
    })
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/config-sms-charges")
    ResponseEntity<ApiResponse<SMSChargeResponse>> configureSMSCharges(@Valid @RequestBody SMSChargeFeeRequest smsChargeFeeRequest){

        SMSChargeResponse smsChargeResponse = profileService.configureSMSCharge(smsChargeFeeRequest);
        ApiResponse<SMSChargeResponse> response = new ApiResponse<>(smsChargeResponse, "Data created successfully", true);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @ApiOperation(
            value = "Check SMS Alert Status",
            notes = "Config SMS Charge Fee: User can check status of sms alert")
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 400, message = MESSAGE_400),
            @io.swagger.annotations.ApiResponse(code = 422, message = MESSAGE_422)
    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/sms-alert/status/{phoneNumber}")
    ResponseEntity<ApiResponse<ToggleSMSResponse>> getSMSAlertStatus(@Valid @ApiParam(example = "08054354344") @PathVariable String phoneNumber){

        ToggleSMSResponse toggleSMSResponse = profileService.getSMSAlertStatus(phoneNumber);
        ApiResponse<ToggleSMSResponse> response = new ApiResponse<>(toggleSMSResponse, "Data created successfully", true);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    @ApiOperation(
            value = "Config SMS Charge Fee",
            notes = "Config SMS Charge Fee: Admin can create sms alert charge")
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 400, message = MESSAGE_400),
            @io.swagger.annotations.ApiResponse(code = 422, message = MESSAGE_422)
    })
    @ResponseStatus(HttpStatus.OK)
    @PutMapping("/sms-charges/toggle/{id}")
    ResponseEntity<ApiResponse<SMSChargeResponse>> toggleSMSCharges(@Valid @ApiParam(example = "1") @PathVariable Long id){

        SMSChargeResponse smsChargeResponse = profileService.toggleSMSCharge(id);
        ApiResponse<SMSChargeResponse> response = new ApiResponse<>(smsChargeResponse, "Data created successfully", true);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    @ApiOperation(
            value = "View Active SMS Charge",
            notes = "View Active SMS Charge: User can check status of sms alert")
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 400, message = MESSAGE_400),
            @io.swagger.annotations.ApiResponse(code = 422, message = MESSAGE_422)
    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/view-active-sms-charge")
    ResponseEntity<ApiResponse<SMSChargeResponse>> getActiveSMSCharge(){
        SMSChargeResponse toggleSMSResponse = profileService.getActiveSMSCharge();
        ApiResponse<SMSChargeResponse> response = new ApiResponse<>(toggleSMSResponse, "Data created successfully", true);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @ApiOperation(
            value = "Get Profile By Referral Code",
            notes = "View Active SMS Charge: Admin can validate that the referral code belongs to another user")
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 400, message = MESSAGE_400),
            @io.swagger.annotations.ApiResponse(code = 422, message = MESSAGE_422)
    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/get-user-profile/{referralCode}")
    ResponseEntity<ApiResponse<UserProfileResponse>> getProfileByReferralCode(@PathVariable String referralCode){
        UserProfileResponse userProfileResponse = profileService.getProfileByReferralCode(referralCode);
        ApiResponse<UserProfileResponse> response = new ApiResponse<>(userProfileResponse, "Data created successfully", true);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private String getBaseUrl(HttpServletRequest request) {
        return "http://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath();
    }





}
