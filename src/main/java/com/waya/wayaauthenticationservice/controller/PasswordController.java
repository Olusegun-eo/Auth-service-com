package com.waya.wayaauthenticationservice.controller;

import com.waya.wayaauthenticationservice.pojo.password.ChangePasswordPojo;
import com.waya.wayaauthenticationservice.pojo.password.PasswordPojo;
import com.waya.wayaauthenticationservice.pojo.password.ResetPasswordPojo;
import com.waya.wayaauthenticationservice.service.PasswordService;
import com.waya.wayaauthenticationservice.util.ValidPhone;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Email;

@RestController
@RequestMapping("/api/v1/password")
@Api(tags = {"Password Resource"})
@Tag(name = "PASSWORD RESOURCE", description = "REST API for Password Service.")
@CrossOrigin
@Validated
public class PasswordController {

    private PasswordService passwordService;

    public PasswordController(PasswordService passwordService) {
        this.passwordService = passwordService;
    }

    @ApiOperation(value = "forgot password post request", notes = "forgot password post Request",tags = { "PASSWORD RESOURCE" })
    @PostMapping("/forgot-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordPojo resetPasswordPojo) {
        return passwordService.resetPassword(resetPasswordPojo);
    }

    @ApiOperation(value = "Send OTP to email, this is for forgot password post Request", notes = "Send OTP to email, this is for forgot password post Request",tags = { "PASSWORD RESOURCE" })
    @GetMapping("/forgot-password/byEmail")
    public ResponseEntity<?> sendPasswordResetOTPEmail(@RequestParam("email") @Email String email,
                                                       @RequestParam(name = "redirectUrl", required = false) String redirectUrl) {
        return passwordService.sendPasswordResetOTPByEmail(email, redirectUrl);
    }

    @ApiOperation(value = "Send OTP to PhoneNumber, this is for forgot password post Request", notes = "Send OTP to Phone, this is for forgot password post Request",tags = { "PASSWORD RESOURCE" })
    @GetMapping("/forgot-password/byPhone")
    public ResponseEntity<?> sendPasswordResetOTPPhone(@RequestParam("phoneNumber") @ValidPhone String phoneNumber) {
        return passwordService.sendPasswordResetOTPByPhoneNumber(phoneNumber);
    }

    @ApiOperation(value = "Send OTP to email, this is for Change password post Request", notes = "Send OTP to email, this is for Change password post Request",tags = { "PASSWORD RESOURCE" })
    @GetMapping("/change-password/byEmail")
    public ResponseEntity<?> sendPasswordForgotOTPEmail(@RequestParam("email") @Email String email,
                                                        @RequestParam(name = "redirectUrl", required = false) String redirectUrl) {
        return passwordService.sendPasswordChangeOTPByEmail(email, redirectUrl);
    }

    @ApiOperation(value = "Send OTP to PhoneNumber, this is for Change password post Request", notes = "Send OTP to Phone, this is for Change password post Request",tags = { "PASSWORD RESOURCE" })
    @GetMapping("/change-password/byPhone")
    public ResponseEntity<?> sendPasswordForgotOTPPhone(@RequestParam("phoneNumber") @ValidPhone String phoneNumber) {
        return passwordService.sendPasswordChangeOTPByPhoneNumber(phoneNumber);
    }

    @ApiOperation(value = "Change password post Request", notes = "Change password post Request",tags = { "PASSWORD RESOURCE" })
    @PostMapping("/change-password")
    public ResponseEntity<?> changeForgotPassword(@Valid @RequestBody PasswordPojo passwordPojo) {
        return passwordService.changePassword(passwordPojo);
    }

    @ApiOperation(value = "Change password Get Request", notes = "Change password Get Request",tags = { "PASSWORD RESOURCE" })
    @GetMapping("/change")
    public String sendChangePassword() {
        return "to return the HTML Page for Change Password";
    }
    
    @ApiOperation(value = "Change password post Request {ADMIN ACTION}", notes = "Change password post Request",tags = { "PASSWORD RESOURCE" })
    @PostMapping("/change")
    public ResponseEntity<?> changePassword(@Valid @RequestBody ChangePasswordPojo passwordPojo) {
        return passwordService.changePassword(passwordPojo);
    }

    @ApiOperation(value = "Reset password post Request {ADMIN ACTION}", notes = "Reset password post Request",tags = { "PASSWORD RESOURCE" })
    @PostMapping("/reset")
    @PreAuthorize(value = "hasAuthority('ROLE_APP_ADMIN') and @userSecurity.useHierarchy(#pojo.phoneOrEmail, authentication)")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ChangePasswordPojo pojo) {
        return passwordService.resetPassword(pojo);
    }

}
