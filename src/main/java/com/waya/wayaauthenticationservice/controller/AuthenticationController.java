package com.waya.wayaauthenticationservice.controller;

import com.waya.wayaauthenticationservice.pojo.*;
import com.waya.wayaauthenticationservice.service.AuthenticationService;
import com.waya.wayaauthenticationservice.service.impl.AuthenticationServiceImpl;
import io.swagger.annotations.*;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "AUTH", description = "User Authentication Service API")
public class AuthenticationController {

    @Autowired
    private AuthenticationService authenticationServiceImpl;

    @ApiOperation(value="Personal User Registration",tags = { "AUTH" })
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Response Headers")})
    @PostMapping(path = "/create", consumes = {MediaType.APPLICATION_JSON_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> create(@RequestBody UserPojo user) {
        return authenticationServiceImpl.createUser(user);
    }


    @ApiOperation(value="Corporate User Registration" ,tags = { "AUTH" })
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Response Headers")})
    @PostMapping(path = "/create-corporate", consumes = {MediaType.APPLICATION_JSON_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> createCorporate(@RequestBody CorporateUserPojo user) {
        return authenticationServiceImpl.createCorporateUser(user);
    }


    @ApiOperation(value="Verify phone number with OTP",tags = { "AUTH" })
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Response Headers")})
    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOTP(@RequestBody OTPPojo otpPojo) {
        return authenticationServiceImpl.verifyOTP(otpPojo);
    }

    @ApiOperation(value="Verify email" ,tags = { "AUTH" })
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Response Headers")})
    @PostMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestBody EmailPojo emailPojo) {
        return authenticationServiceImpl.verifyEmail(emailPojo);
    }


    @ApiOperation(value = "Password Change (Service consumption only. Do not Use)", notes = "This is meant for service consumption" ,tags = { "AUTH" })
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Response Headers")})
    @PostMapping("/password-change")
    public ResponseEntity<?> changePass(@RequestBody PasswordPojo passwordPojo) {
        return authenticationServiceImpl.changePassword(passwordPojo);
    }

    @ApiOperation(value = "Forgot Password (Service consumption only. Do not Use)", notes = "This is meant for service consumption" ,tags = { "AUTH" })
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Response Headers")})
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPass(@RequestBody PasswordPojo2 passwordPojo) {
        return authenticationServiceImpl.forgotPassword(passwordPojo);
    }

    @ApiOperation(value = "Pin Change (Service consumption only. Do not Use)", notes = "This is meant for service consumption" ,tags = { "AUTH" })
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Response Headers")})
    @PostMapping("/pin-change")
    public ResponseEntity<?> changePin(@RequestBody PinPojo2 pinPojo) {
        return authenticationServiceImpl.changePin(pinPojo);
    }

    @ApiOperation(value = "Forgot Pin (Service consumption only. Do not Use)", notes = "This is meant for service consumption" ,tags = { "AUTH" })
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Response Headers")})
    @PostMapping("/forgot-pin")
    public ResponseEntity<?> forgotPin(@RequestBody PinPojo pinPojo) {
        return authenticationServiceImpl.forgotPin(pinPojo);
    }

    @ApiOperation(value="User login" ,tags = { "AUTH" })
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Response Headers")})
    @PostMapping("/login")
    public void login(@RequestBody LoginDetailsPojo loginRequestModel) {
        throw new IllegalStateException("This Method should not be called!");
    }

    @ApiOperation(value = "Pin Creation", notes = "This endpoint help user create transaction PIN" ,tags = { "AUTH" })
    @ApiImplicitParams({ @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
    @PostMapping("/create-pin")
    public ResponseEntity<?> createPin(@RequestBody PinPojo pinPojo) {
        return authenticationServiceImpl.createPin(pinPojo);
    }


    @ApiOperation(value = "User Validation (Service consumption only. Do not Use)", notes = "This endpoint help validate user and is meant for service consumption only",tags = { "AUTH" })
    @ApiImplicitParams({ @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
    @PostMapping("/validate-user")
    public ResponseEntity<?> validateUser() {
        return authenticationServiceImpl.validateUser();
    }

    @ApiOperation(value = "PIN verification (Service consumption only. Do not Use)", notes = "This endpoint help validate user by Pin and is meant for service consumption only" ,tags = { "AUTH" })
    @GetMapping("/validate-pin/{userId}/{pin}")
    public ResponseEntity<?> validateUserByPin(@PathVariable Long userId,@PathVariable int pin) {
        return authenticationServiceImpl.validatePin(userId, pin);
    }

    @ApiOperation(value = "PIN verification for user consumption", notes = "This endpoint help validate user by Pin by Authorisation token" ,tags = { "AUTH" })
    @ApiImplicitParams({ @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
    @GetMapping("/validate-pin/{pin}")
    public ResponseEntity<?> validateUserByPin(@PathVariable int pin) {
        return authenticationServiceImpl.validatePinFromUser(pin);
    }


    @ApiOperation(value="Resend OTP to Phone" ,tags = { "AUTH" })
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Response Headers")})
    @GetMapping("/resend-otp/{phoneNumber}/{email}")
    public ResponseEntity<?> resendOTP(@PathVariable String phoneNumber, String email) {
        return authenticationServiceImpl.resendOTP(phoneNumber, email);
    }

    @ApiOperation(value="Resend Verification Email", tags = { "AUTH" })
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Response Headers")})
    @GetMapping("/resend-otp-mail/{email}/{userId}")
    public ResponseEntity<?> resendOTPEmail(@PathVariable String email, String userId) {
        return authenticationServiceImpl.resendVerificationMail(email, userId);
    }


}
