package com.waya.wayaauthenticationservice.controller;


import com.waya.wayaauthenticationservice.response.OTPVerificationResponse;
import com.waya.wayaauthenticationservice.service.SMSTokenService;
import io.swagger.annotations.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import com.waya.wayaauthenticationservice.util.profile.ApiResponse;
import javax.validation.Valid;
import static com.waya.wayaauthenticationservice.util.Constant.MESSAGE_400;
import static com.waya.wayaauthenticationservice.util.Constant.MESSAGE_422;

@Api(tags = {"OTP Resource"})
@SwaggerDefinition(tags = {
        @Tag(name = "OTP Resource", description = "REST API for OTP.")
})
@CrossOrigin
@RestController
public class OTPController {

    private final SMSTokenService smsTokenService;

    public OTPController(SMSTokenService smsTokenService) {
        this.smsTokenService = smsTokenService;
    }

    @ApiOperation(
            value = "${api.otp.send-otp.description}",
            notes = "${api.otp.send-otp.notes}")
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 400, message = MESSAGE_400),
            @io.swagger.annotations.ApiResponse(code = 422, message = MESSAGE_422)
    })
    @GetMapping(path = "otp/{phoneNumber}/{email}")
    public ResponseEntity<ApiResponse<Boolean>> getOTP(@PathVariable @Valid String phoneNumber,
                                                      @PathVariable @Valid String email) {
       boolean status = smsTokenService.sendSMSOTP(phoneNumber, email);
        return new ResponseEntity<>(new ApiResponse<>(status, "your otp has been sent to your phone",
                true), HttpStatus.ACCEPTED);
    }

    /**
     * endpoint to verify an OTP
     *
     * @param phoneNumber phone number
     * @param otp         otp
     * @return Object
     */
    @ApiOperation(
            value = "${api.otp.verify-otp.description}",
            notes = "${api.otp.verify-otp.notes}")
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 400, message = MESSAGE_400),
            @io.swagger.annotations.ApiResponse(code = 422, message = MESSAGE_422)
    })
    @GetMapping(path = "otp-verify/{phoneNumber}/{otp}")
    public ApiResponse<OTPVerificationResponse> verifyOTP(@PathVariable @Valid String phoneNumber,
                                                          @PathVariable Integer otp) {
        return smsTokenService.verifySMSOTP(phoneNumber, otp);
    }
}