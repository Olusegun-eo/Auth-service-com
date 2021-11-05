package com.waya.wayaauthenticationservice.controller;

import com.waya.wayaauthenticationservice.entity.ReferralCode;
import com.waya.wayaauthenticationservice.response.ReferralCodeResponse;
import com.waya.wayaauthenticationservice.service.ManageReferralService;
import com.waya.wayaauthenticationservice.service.ReferralService;
import com.waya.wayaauthenticationservice.util.Constant;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.waya.wayaauthenticationservice.response.ApiResponseBody;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;
import java.util.Map;

@Tag(name = "REFERRAL RESOURCE",  description = "REST API for Referral Service API")
@CrossOrigin
@RestController
@RequestMapping("/api/v1/referral")
public class ReferralCodeController {
    private final ReferralService referralService;
    private final ManageReferralService manageReferralService;

    @Autowired
    public ReferralCodeController(ReferralService referralService, ManageReferralService manageReferralService) {
        this.referralService = referralService;
        this.manageReferralService = manageReferralService;
    }

    @ApiOperation( value = "referral-code/{userId}", notes = "", tags = {"REFERRAL RESOURCE"})
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 400, message = Constant.MESSAGE_400),
            @io.swagger.annotations.ApiResponse(code = 422, message = Constant.MESSAGE_422)
    })
    @GetMapping("referral-code/{userId}")
    public ResponseEntity<ApiResponseBody<ReferralCodeResponse>> getReferralCode(@PathVariable String userId) {
        ReferralCodeResponse referralCodeResponse = referralService.getReferralCode(userId);
        ApiResponseBody<ReferralCodeResponse> response = new ApiResponseBody<>(referralCodeResponse, "retrieved data successfully", true);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @ApiOperation( value = "migrate-referral-code", notes = "", tags = {"REFERRAL RESOURCE"})
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 400, message = Constant.MESSAGE_400),
            @io.swagger.annotations.ApiResponse(code = 422, message = Constant.MESSAGE_422)
    })
    @PostMapping("migrate-referral-code")
    public ResponseEntity<ApiResponseBody<List<ReferralCode>>> getReferralCode() throws Exception {
        List<ReferralCode> referralCodeResponse = referralService.migrateReferralCode();
        ApiResponseBody<List<ReferralCode>> response = new ApiResponseBody<>(referralCodeResponse, "retrieved data successfully", true);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @ApiOperation( value = "profile-with-five-transactions", notes = "", tags = {"REFERRAL RESOURCE"})
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 400, message = Constant.MESSAGE_400),
            @io.swagger.annotations.ApiResponse(code = 422, message = Constant.MESSAGE_422)
    })
    @GetMapping("/profile-with-five-transactions/user/{userId}")
    public ResponseEntity<ApiResponseBody<ReferralCodeResponse>> getUsersWithUpToFiveTransactions(@PathVariable String userId, @ApiIgnore @RequestAttribute(Constant.TOKEN) String token) {
        ReferralCodeResponse referralCodeResponse = (ReferralCodeResponse) referralService.getUsersWithUpToFiveTransactions(userId, token);
        ApiResponseBody<ReferralCodeResponse> response = new ApiResponseBody<>(referralCodeResponse, "retrieved data successfully", true);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @ApiOperation( value = "GET USERS THAT HAVE BEEN REFERRED", notes = "", tags = {"REFERRAL RESOURCE"})
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 400, message = Constant.MESSAGE_400),
            @io.swagger.annotations.ApiResponse(code = 422, message = Constant.MESSAGE_422)
    })
    @GetMapping("/get-users-that-have-been-referred/{referralCode}")
    public ResponseEntity<ApiResponseBody<Map<String, Object>>> getUserThanHaveBeenReferred(
            @PathVariable String referralCode,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Map<String, Object> referralCodeResponse = manageReferralService.getUserThatHaveBeenReferred(referralCode,page,size);
        ApiResponseBody<Map<String, Object>> response = new ApiResponseBody<>(referralCodeResponse, "Retrieved data successfully", true);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @ApiOperation( value = "GET USERS TOTAL REFERRAL EARNINGS", notes = "", tags = {"REFERRAL RESOURCE"})
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 400, message = Constant.MESSAGE_400),
            @io.swagger.annotations.ApiResponse(code = 422, message = Constant.MESSAGE_422)
    })
    @GetMapping("/get-users-total-referral-earnings/{userId}")
    public ResponseEntity<ApiResponseBody<Double>> getUserThanHaveBeenReferred(@PathVariable String userId ) {
        Double referralCodeResponse = manageReferralService.getReferralBonusEarning(userId);
        ApiResponseBody<Double> response = new ApiResponseBody<>(referralCodeResponse, "Retrieved data successfully", true);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }





}
