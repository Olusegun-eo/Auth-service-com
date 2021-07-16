package com.waya.wayaauthenticationservice.controller;

import com.waya.wayaauthenticationservice.response.ReferralCodeResponse;
import com.waya.wayaauthenticationservice.service.ReferralService;
import com.waya.wayaauthenticationservice.util.Constant;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.waya.wayaauthenticationservice.response.ApiResponse;

@Tag(name = "REFERRAL RESOURCE",  description = "REST API for Referral Service API")
@CrossOrigin
@RestController
@RequestMapping("/api/v1/referral")
public class ReferralCodeController {
    private final ReferralService referralService;

    public ReferralCodeController(ReferralService referralService) {
        this.referralService = referralService;
    }

    @ApiOperation( value = "referral-code/{userId}" )
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 400, message = Constant.MESSAGE_400),
            @io.swagger.annotations.ApiResponse(code = 422, message = Constant.MESSAGE_422)
    })
    @GetMapping("referral-code/{userId}")
    public ResponseEntity<ApiResponse<ReferralCodeResponse>> getReferralCode(@PathVariable String userId) {
        ReferralCodeResponse referralCodeResponse = referralService.getReferralCode(userId);
        ApiResponse<ReferralCodeResponse> response = new ApiResponse<>(referralCodeResponse, "retrieved data successfully", true);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
