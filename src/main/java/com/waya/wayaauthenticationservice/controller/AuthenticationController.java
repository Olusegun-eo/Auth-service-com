package com.waya.wayaauthenticationservice.controller;

import com.waya.wayaauthenticationservice.pojo.*;
import com.waya.wayaauthenticationservice.service.impl.AuthenticationServiceImpl;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {

    @Autowired
    private AuthenticationServiceImpl authenticationServiceImpl;

    @ApiOperation("Personal User Registration")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Response Headers")})
    @PostMapping(path = "/create", consumes = {MediaType.APPLICATION_JSON_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> create(@RequestBody UserPojo user) {
        return authenticationServiceImpl.createUser(user);
    }


    @ApiOperation("Corporate User Registration")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Response Headers")})
    @PostMapping(path = "/create-corporate", consumes = {MediaType.APPLICATION_JSON_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> createCorporate(@RequestBody CorporateUserPojo user) {
        return authenticationServiceImpl.createCorporateUser(user);
    }


    @ApiOperation("Verify phone number with OTP")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Response Headers")})
    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOTP(@RequestBody OTPPojo otpPojo) {
        return authenticationServiceImpl.verifyOTP(otpPojo);
    }

    @ApiOperation("Verify email")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Response Headers")})
    @PostMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestBody EmailPojo emailPojo) {
        return authenticationServiceImpl.verifyEmail(emailPojo);
    }


    @ApiOperation(value = "Password Change (Service consumption only. Do not Use)", notes = "This is meant for service consumption")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Response Headers")})
    @PostMapping("/password-change")
    public ResponseEntity<?> changePass(@RequestBody PasswordPojo passwordPojo) {
        return authenticationServiceImpl.changePassword(passwordPojo);
    }

    @ApiOperation(value = "Forgot Password (Service consumption only. Do not Use)", notes = "This is meant for service consumption")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Response Headers")})
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPass(@RequestBody PasswordPojo2 passwordPojo) {
        return authenticationServiceImpl.forgotPassword(passwordPojo);
    }

    @ApiOperation(value = "Pin Change (Service consumption only. Do not Use)", notes = "This is meant for service consumption")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Response Headers")})
    @PostMapping("/pin-change")
    public ResponseEntity<?> changePin(@RequestBody PinPojo2 pinPojo) {
        return authenticationServiceImpl.changePin(pinPojo);
    }

    @ApiOperation(value = "Forgot Pin (Service consumption only. Do not Use)", notes = "This is meant for service consumption")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Response Headers")})
    @PostMapping("/forgot-pin")
    public ResponseEntity<?> forgotPin(@RequestBody PinPojo pinPojo) {
        return authenticationServiceImpl.forgotPin(pinPojo);
    }

    @ApiOperation("User login")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Response Headers")})
    @PostMapping("/login")
    public void login(@RequestBody LoginDetailsPojo loginRequestModel) {
        throw new IllegalStateException("This Method should not be called!");
    }

    @ApiOperation(value = "Pin Creation", notes = "This endpoint help user create transaction PIN")
    @ApiImplicitParams({ @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
    @PostMapping("/create-pin")
    public ResponseEntity<?> createPin(@RequestBody PinPojo pinPojo) {
        return authenticationServiceImpl.createPin(pinPojo);
    }


    @ApiOperation(value = "User Validation (Service consumption only. Do not Use)", notes = "This endpoint help validate user and is meant for service consumption only")
    @ApiImplicitParams({ @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
    @PostMapping("/validate-user")
    public ResponseEntity<?> validateUser() {
        return authenticationServiceImpl.validateUser();
    }

    @ApiOperation(value = "PIN verification (Service consumption only. Do not Use)", notes = "This endpoint help validate user by Pin and is meant for service consumption only")
    @GetMapping("/validate-pin/{userId}/{pin}")
    public ResponseEntity<?> validateUserByPin(@PathVariable Long userId,@PathVariable int pin) {
        return authenticationServiceImpl.validatePin(userId, pin);
    }


    @ApiOperation("Resend OTP to Phone")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Response Headers")})
    @GetMapping("/resend-otp/{phoneNumber}/{email}")
    public ResponseEntity<?> resendOTP(@PathVariable String phoneNumber, String email) {
        return authenticationServiceImpl.resendOTP(phoneNumber, email);
    }

    @ApiOperation("Resend Verification Email")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Response Headers")})
    @GetMapping("/resend-otp-mail/{email}/{userId}")
    public ResponseEntity<?> resendOTPEmail(@PathVariable String email, String userId) {
        return authenticationServiceImpl.resendVerificationMail(email, userId);
    }


}
