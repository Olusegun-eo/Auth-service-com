package com.waya.wayaauthenticationservice.controller;

import com.waya.wayaauthenticationservice.response.ApiResponseBody;
import com.waya.wayaauthenticationservice.response.ReferralCodeResponse;
import com.waya.wayaauthenticationservice.service.ProfileService;
import com.waya.wayaauthenticationservice.util.Constant;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.Map;

@Tag(name = "PROFILE ADMIN RESOURCE", description = "REST API for Referral Admin Service API")
@CrossOrigin
@RestController
@RequestMapping("/api/v1/referral/admin")
@Validated
public class ReferralAdminController {


    private final ProfileService profileService;

    @Autowired
    public ReferralAdminController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @ApiOperation( value = "profile-with-five-transactions/user/{userId}", notes = "", tags = {"REFERRAL RESOURCE"})
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 400, message = Constant.MESSAGE_400),
            @io.swagger.annotations.ApiResponse(code = 422, message = Constant.MESSAGE_422)
    })
    @GetMapping("/filter-users/{value}")
    public ResponseEntity<ApiResponseBody<Map<String, Object>>> getUsersWithUpToFiveTransactions(
            @PathVariable String value,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Map<String, Object> referralCodeResponse =  profileService.getUsersWithTheirReferralsByPhoneNumber(value,page,size);
        ApiResponseBody<Map<String, Object>> response = new ApiResponseBody<>(referralCodeResponse, "Retrieved data successfully", true);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    @ApiOperation( value = "GET REFERRALS USERS", notes = "", tags = {"REFERRAL RESOURCE"})
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 400, message = Constant.MESSAGE_400),
            @io.swagger.annotations.ApiResponse(code = 422, message = Constant.MESSAGE_422)
    })
    @GetMapping("/get-referral-users")
    public ResponseEntity<ApiResponseBody<Map<String, Object>>> getUsersWithTheirReferrals(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Map<String, Object> referralCodeResponse =  profileService.getUsersWithTheirReferrals(page,size);
        ApiResponseBody<Map<String, Object>> response = new ApiResponseBody<>(referralCodeResponse, "Retrieved data successfully", true);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }






}
