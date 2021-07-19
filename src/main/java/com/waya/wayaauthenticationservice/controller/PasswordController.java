package com.waya.wayaauthenticationservice.controller;

import javax.validation.Valid;
import javax.validation.constraints.Email;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.waya.wayaauthenticationservice.pojo.password.PasswordPojo;
import com.waya.wayaauthenticationservice.pojo.password.ResetPasswordPojo;
import com.waya.wayaauthenticationservice.service.PasswordService;
import com.waya.wayaauthenticationservice.util.ValidPhone;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/api/v1/password")
@Api(tags = {"Password Resource"})
@Tag(name = "Password Resource", description = "REST API for Password Service.")
@CrossOrigin
@Validated
public class PasswordController {

    private PasswordService passwordService;

    public PasswordController(PasswordService passwordService) {
        this.passwordService = passwordService;
    }

    @ApiOperation(value = "forgot password post request", notes = "forgot password post Request")
    @PostMapping("/forgot-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordPojo resetPasswordPojo) {
        return passwordService.resetPassword(resetPasswordPojo);
    }

    @ApiOperation(value = "Send OTP to email, this is for forgot password post Request", notes = "Send OTP to email, this is for forgot password post Request")
    @GetMapping("/forgot-password/byEmail")
    public ResponseEntity<?> sendPasswordResetOTPEmail(@RequestParam("email") @Email String email,
                                                       @RequestParam(name = "redirectUrl", required = false) String redirectUrl) {
        return passwordService.sendPasswordResetOTPByEmail(email, redirectUrl);
    }

    @ApiOperation(value = "Send OTP to PhoneNumber, this is for forgot password post Request", notes = "Send OTP to Phone, this is for forgot password post Request")
    @GetMapping("/forgot-password/byPhone")
    public ResponseEntity<?> sendPasswordResetOTPPhone(@RequestParam("phoneNumber") @ValidPhone String phoneNumber) {
        return passwordService.sendPasswordResetOTPByPhoneNumber(phoneNumber);
    }

    @ApiOperation(value = "Send OTP to email, this is for Change password post Request", notes = "Send OTP to email, this is for Change password post Request")
    @GetMapping("/change-password/byEmail")
    public ResponseEntity<?> sendPasswordForgotOTPEmail(@RequestParam("email") @Email String email,
                                                        @RequestParam(name = "redirectUrl", required = false) String redirectUrl) {
        return passwordService.sendPasswordResetOTPByEmail(email, redirectUrl);
    }

    @ApiOperation(value = "Send OTP to PhoneNumber, this is for Change password post Request", notes = "Send OTP to Phone, this is for Change password post Request")
    @GetMapping("/change-password/byPhone")
    public ResponseEntity<?> sendPasswordForgotOTPPhone(@RequestParam("phoneNumber") @ValidPhone String phoneNumber) {
        return passwordService.sendPasswordResetOTPByPhoneNumber(phoneNumber);
    }

    @ApiOperation(value = "Change password post Request", notes = "Change password post Request")
    @PostMapping("/change-password")
    public ResponseEntity<?> changeForgotPassword(@Valid @RequestBody PasswordPojo passwordPojo) {
        return passwordService.changePassword(passwordPojo);
    }

}
