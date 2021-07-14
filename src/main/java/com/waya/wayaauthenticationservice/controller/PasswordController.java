package com.waya.wayaauthenticationservice.controller;

import com.waya.wayaauthenticationservice.pojo.password.PasswordPojo;
import com.waya.wayaauthenticationservice.pojo.password.NewPinPojo;
import com.waya.wayaauthenticationservice.pojo.password.ChangePINPojo;
import com.waya.wayaauthenticationservice.pojo.password.ResetPasswordPojo;
import com.waya.wayaauthenticationservice.service.PasswordService;
import io.swagger.annotations.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.Email;

@RestController
@RequestMapping("/api/v1/password")
@Api(tags = {"Password Resource"})
@SwaggerDefinition(tags = {
        @Tag(name = "Password Resource", description = "REST API for Password Service.")
})
@CrossOrigin
public class PasswordController {

    private PasswordService passwordService;

    public PasswordController(PasswordService passwordService) {
        this.passwordService = passwordService;
    }

    @ApiOperation(value = "Change password post Request", notes = "Change password post Request")
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordPojo resetPasswordPojo) {
        return passwordService.resetPassword(resetPasswordPojo);
    }

    @ApiOperation(value = "Send OTP to email, this is for forgot password post Request", notes = "Send OTP to email, this is for forgot password post Request")
    @GetMapping("/reset-password/{email}")
    public ResponseEntity<?> sendPasswordResetOTP(@Valid @Email @PathVariable("email") String email, @RequestParam("redirectUrl") final String redirectUrl) {
        return passwordService.sendPasswordResetOTPByEmail(email, redirectUrl);
    }

    @ApiOperation(value = "Forgot password post Request", notes = "Forgot password post Request")
    @PostMapping("/forgot-password")
    public ResponseEntity<?> changeForgotPassword(@Valid @RequestBody PasswordPojo passwordPojo) {
        return passwordService.changePassword(passwordPojo);
    }

    @ApiOperation(value = "Pin Change (Service consumption only. Do not Use)", notes = "This is meant for service consumption", tags = {
            "AUTH"})
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Response Headers")})
    @PostMapping("/pin-change")
    public ResponseEntity<?> changePin(@Valid @RequestBody ChangePINPojo pinPojo) {
        return passwordService.changePin(pinPojo);
    }

}
