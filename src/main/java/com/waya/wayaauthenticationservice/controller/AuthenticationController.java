package com.waya.wayaauthenticationservice.controller;

import com.waya.wayaauthenticationservice.pojo.notification.OTPPojo;
import com.waya.wayaauthenticationservice.pojo.others.EmailPojo;
import com.waya.wayaauthenticationservice.pojo.others.LoginDetailsPojo;
import com.waya.wayaauthenticationservice.pojo.userDTO.BaseUserPojo;
import com.waya.wayaauthenticationservice.pojo.userDTO.CorporateUserPojo;
import com.waya.wayaauthenticationservice.service.AuthenticationService;
import com.waya.wayaauthenticationservice.service.UserService;
import io.swagger.annotations.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mobile.device.Device;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.ws.rs.Produces;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "AUTH", description = "User Authentication Service API")
public class AuthenticationController {

    @Autowired
    UserService userService;

    //public static final String HEADER_STRING = "Authorization";
    @Autowired
    private AuthenticationService authenticationServiceImpl;

    @ApiOperation(value = "Personal User Registration", tags = {"AUTH"})
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Response Headers")})
    @PostMapping(path = "/create", consumes = {MediaType.APPLICATION_JSON_VALUE}, produces = {
            MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> create(@Valid @RequestBody BaseUserPojo user, HttpServletRequest request, Device device) {
        return authenticationServiceImpl.createUser(user, request, device, false);
    }

    @ApiOperation(value = "Corporate User Registration", tags = {"AUTH"})
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Response Headers")})
    @PostMapping(path = "/create-corporate", consumes = {MediaType.APPLICATION_JSON_VALUE}, produces = {
            MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> createCorporate(@Valid @RequestBody CorporateUserPojo user, HttpServletRequest request,
                                             Device device) {
        return authenticationServiceImpl.createCorporateUser(user, request, device, false);
    }

    @ApiOperation(value = "Verify Account with OTP", tags = {"AUTH"})
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Response Headers")})
    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyAccount(@RequestBody OTPPojo otpPojo) {
        return authenticationServiceImpl.verifyAccountCreation(otpPojo);
    }

    @ApiOperation(value = "Verify phone number with OTP", tags = {"AUTH"})
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Response Headers")})
    @PostMapping("/verify-phone")
    public ResponseEntity<?> verifyOTP(@RequestBody OTPPojo otpPojo) {
        return authenticationServiceImpl.verifyPhoneUsingOTP(otpPojo);
    }

    @ApiOperation(value = "Verify email", tags = {"AUTH"})
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Response Headers")})
    @PostMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestBody EmailPojo emailPojo) {
        return authenticationServiceImpl.verifyEmail(emailPojo);
    }

    @ApiOperation(value = "User login", tags = {"AUTH"})
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Response Headers")})
    @PostMapping("/login")
    public void login(@RequestBody LoginDetailsPojo loginRequestModel) {
        throw new IllegalStateException("This Method should not be called!");
    }

    @GetMapping("/social")
    @Produces(javax.ws.rs.core.MediaType.TEXT_HTML)
    @ApiOperation(value = "Github login", tags = {"AUTH"})
    public String login() {
        // return "Log in with <a
        // href=\"http://localhost:8059/oauth2/callback/authorization/github\">GitHub</a>";
        return "<a href=\"http://localhost:8080/oauth2/authorize/google\"> Log in with Google</a><br><br><a href=\"http://localhost:8080/oauth2/authorize/facebook\">Log in with Facebook</a><br><br><a href=\"http://localhost:8080/oauth2/authorize/github\">Log in with Github</a>";
    }

    @ApiOperation(value = "User Validation (Service consumption only. Do not Use)", notes = "This endpoint help validate user and is meant for service consumption only", tags = {
            "AUTH"})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true)})
    @RequestMapping(value = "/validate-user", method = RequestMethod.POST, consumes = {MediaType.APPLICATION_JSON_VALUE,
            MediaType.MULTIPART_FORM_DATA_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> validateUser() {
        return authenticationServiceImpl.validateUser();
    }

    @ApiOperation(value = "Resend OTP to Phone", tags = {"AUTH"})
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Response Headers")})
    @GetMapping("/resend-otp/{phoneNumber}")
    public ResponseEntity<?> resendOTPPhone(@PathVariable("phoneNumber") String phoneNumber) {
        return authenticationServiceImpl.resendOTPPhone(phoneNumber);
    }

    @ApiOperation(value = "Resend Verification Email", tags = {"AUTH"})
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Response Headers")})
    @GetMapping("/resend-otp-mail/{email}")
    public ResponseEntity<?> resendOTPEmail(@PathVariable String email, final HttpServletRequest request) {
        return authenticationServiceImpl.resendVerificationMail(email, getBaseUrl(request));
    }

    @ApiOperation(value = "Check if user is an admin: (Internal Consumption only)", tags = {"AUTH"})
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Response Headers")})
    @GetMapping("/is-user-admin/{userId}")
    public ResponseEntity<?> isUserAdmin(@PathVariable Long userId) {
        return userService.isUserAdmin(userId);
    }

    private String getBaseUrl(HttpServletRequest request) {
        return "http://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath();
    }
}
