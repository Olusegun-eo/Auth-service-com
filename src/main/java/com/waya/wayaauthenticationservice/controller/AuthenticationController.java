package com.waya.wayaauthenticationservice.controller;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.ws.rs.Produces;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mobile.device.Device;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.waya.wayaauthenticationservice.enums.Type;
import com.waya.wayaauthenticationservice.pojo.notification.OTPPojo;
import com.waya.wayaauthenticationservice.pojo.others.LoginDetailsPojo;
import com.waya.wayaauthenticationservice.pojo.userDTO.BaseUserPojo;
import com.waya.wayaauthenticationservice.pojo.userDTO.CorporateUserPojo;
import com.waya.wayaauthenticationservice.service.AuthenticationService;
import com.waya.wayaauthenticationservice.service.UserService;
import com.waya.wayaauthenticationservice.util.CustomValidator;
import com.waya.wayaauthenticationservice.util.ValidPhone;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "AUTH", description = "User Authentication Service API")
@Validated
public class AuthenticationController {

	@Autowired
	UserService userService;

	@Value("${api.server.deployed}")
	private String urlRedirect;

	@Autowired
	private AuthenticationService authenticationServiceImpl;

	// @ApiOperation(value = "Personal User Registration", tags = {"AUTH"})
	@ApiOperation(value = "${api.auth.create.description}", notes = "${api.auth.create.notes}", tags = { "AUTH" })
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Response Headers") })
	@PostMapping(path = "/create", consumes = { MediaType.APPLICATION_JSON_VALUE }, produces = {
			MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<?> create(@Valid @RequestBody BaseUserPojo user, HttpServletRequest request, Device device) {
		user.setWayaAdmin(false);
		return authenticationServiceImpl.createUser(user, request, device, false);
	}

	// @ApiOperation(value = "Corporate User Registration", tags = {"AUTH"})
	@ApiOperation(value = "${api.auth.create-corporate.description}", notes = "${api.auth.create-corporate.notes}", tags = {
			"AUTH" })
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Response Headers") })
	@PostMapping(path = "/create-corporate", consumes = { MediaType.APPLICATION_JSON_VALUE }, produces = {
			MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<?> createCorporate(@Valid @RequestBody CorporateUserPojo user, HttpServletRequest request,
			Device device) {
		return authenticationServiceImpl.createCorporateUser(user, request, device, false);
	}

	@ApiOperation(value = "${api.auth.verify-otp.description}", notes = "${api.auth.verify-otp.notes}", tags = {
			"AUTH" })
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Response Headers") })
	@PostMapping("/verify-otp")
	public ResponseEntity<?> verifyAccount(@Valid @RequestBody OTPPojo otpPojo) {
		return authenticationServiceImpl.verifyAccountCreation(otpPojo);
	}

	@ApiOperation(value = "Resend OTP for Account Verification", notes = "See POjo Object for what to pass", tags = {
			"AUTH" })
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Response Headers") })
	@GetMapping("/resend-otp/signup/{emailOrPhoneNumber}")
	public ResponseEntity<?> resendOTP(
			@PathVariable("emailOrPhoneNumber") @CustomValidator(message = "Has to be either a valid Email or PhoneNumber", type = Type.EMAIL_OR_PHONE) String emailOrPhoneNumber,
			final HttpServletRequest request) {
		return authenticationServiceImpl.resendOTPForAccountVerification(emailOrPhoneNumber, getBaseUrl(request));
	}

	@ApiOperation(value = "${api.auth.verify-phone.description}", notes = "${api.auth.verify-phone.notes}", tags = {
			"AUTH" })
	// @ApiOperation(value = "Verify phone number with OTP", tags = {"AUTH"})
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Response Headers") })
	@PostMapping("/verify-phone")
	public ResponseEntity<?> verifyOTP(@Valid @RequestBody OTPPojo otpPojo) {
		return authenticationServiceImpl.verifyPhoneUsingOTP(otpPojo);
	}

	@ApiOperation(value = "${api.auth.verify-email.description}", notes = "${api.auth.verify-email.notes}", tags = {
			"AUTH" })
	// @ApiOperation(value = "Verify email", tags = {"AUTH"})
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Response Headers") })
	@PostMapping("/verify-email")
	public ResponseEntity<?> verifyEmail(@Valid @RequestBody OTPPojo otpPojo) {
		return authenticationServiceImpl.verifyEmail(otpPojo);
	}

	@ApiOperation(value = "${api.auth.login.description}", notes = "${api.auth.login.notes}", tags = { "AUTH" })
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Response Headers") })
	@PostMapping("/login")
	public void login(@Valid @RequestBody LoginDetailsPojo loginRequestModel) {
		throw new IllegalStateException("This Method should not be called!");
	}

	@GetMapping("/social")
	@Produces(javax.ws.rs.core.MediaType.TEXT_HTML)
	@ApiOperation(value = "${api.auth.social.description}", notes = "${api.auth.social.notes}", tags = { "AUTH" })
	public String login() {
		// return "Log in with <a
		// href=\"http://localhost:8059/oauth2/callback/authorization/github\">GitHub</a>";
		return "<a href=\"http://localhost:8080/oauth2/authorize/google\"> Log in with Google</a><br><br><a href=\"http://localhost:8080/oauth2/authorize/facebook\">Log in with Facebook</a><br><br><a href=\"http://localhost:8080/oauth2/authorize/github\">Log in with Github</a>";
	}

	@ApiOperation(value = "Resend OTP to Phone", notes = "See POjo Object for what to pass", tags = { "AUTH" })
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Response Headers") })
	@GetMapping("/resend-otp/{phoneNumber}")
	public ResponseEntity<?> resendOTPPhone(@PathVariable("phoneNumber") @ValidPhone String phoneNumber) {
		return authenticationServiceImpl.resendOTPPhone(phoneNumber);
	}

	// @ApiOperation(value = "Resend Verification Email", tags = {"AUTH"})
	@ApiOperation(value = "Resend OTP to Email Address Provided", notes = "See POjo Object for what to pass", tags = {
			"AUTH" })
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Response Headers") })
	@GetMapping("/resend-otp-mail/{email}")
	public ResponseEntity<?> resendOTPEmail(@PathVariable @Email String email, final HttpServletRequest request) {
		return authenticationServiceImpl.resendVerificationMail(email, getBaseUrl(request));
	}

	@ApiOperation(value = "Check if user is an admin: (Internal Consumption only)", tags = { "AUTH" })
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Response Headers") })
	@GetMapping("/is-user-admin/{userId}")
	public ResponseEntity<?> isUserAdmin(@PathVariable Long userId) {
		return userService.isUserAdmin(userId);
	}

	@ApiOperation(value = "User Validation (Service consumption only. Do not Use)", notes = "This endpoint help validate user and is meant for service consumption only", tags = {
			"AUTH" })
	@ApiImplicitParams({
			@ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@PostMapping("/validate-user")
	public ResponseEntity<?> validateUser() {
		return authenticationServiceImpl.validateUser();
	}

	@ApiOperation(value = "User Validation (Service consumption only. Do not Use)", notes = "This endpoint help validate user and is meant for service consumption only", tags = {
			"AUTH" })
	@PostMapping("/wallet/{userId}/{key}")
	public ResponseEntity<?> validateWalletUserCall(@PathVariable Long userId, @PathVariable String key) {
		return userService.validateWalletUserCall(userId, key);
	}

	private String getBaseUrl(HttpServletRequest request) {
		return "http://" + urlRedirect + ":" + request.getServerPort() + request.getContextPath();
	}
}
