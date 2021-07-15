package com.waya.wayaauthenticationservice.controller;

import com.waya.wayaauthenticationservice.response.ReferralCodeResponse;
import com.waya.wayaauthenticationservice.service.ReferralService;
import com.waya.wayaauthenticationservice.util.Constant;
import io.swagger.annotations.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import com.waya.wayaauthenticationservice.response.ApiResponse;
@Api(tags = {"ReferralCode Resource"})
@SwaggerDefinition(tags = {
        @Tag(name = "ReferralCode Resource", description = "REST API for ReferralCode.")
})
@CrossOrigin
@RestController
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
