package com.waya.wayaauthenticationservice.controller;

import com.waya.wayaauthenticationservice.pojo.password.PasswordPojo;
import com.waya.wayaauthenticationservice.pojo.password.ResetPasswordPojo;
import com.waya.wayaauthenticationservice.response.ResponsePojo;
import com.waya.wayaauthenticationservice.service.PasswordService;
import io.swagger.annotations.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/v1/password")
@Api(tags = {"Password Resource"})
@SwaggerDefinition(tags = {
        @Tag(name = "Password Resource", description = "REST API for Password Service.")
})
@CrossOrigin
//@PreAuthorize(value = "hasRole('ADMIN')")
//@PreAuthorize(value = "@userSecurity.useHierarchy(#email, authentication)")
public class PasswordController {

    private PasswordService passwordService;

    public PasswordController(PasswordService passwordService) {
        this.passwordService = passwordService;
    }

    @ApiOperation(value = "Send OTP to email, this is for forgot password post Request", notes = "Send OTP to email, this is for forgot password post Request")
    @GetMapping("/forgot/password/{email}")
    public ResponseEntity<ResponsePojo> forgotPassword(@PathVariable("email") String email, final String redirectUrl) {
        return ResponseEntity.ok(passwordService.sendPasswordResetOTP(email, redirectUrl));
    }

    @ApiImplicitParams({@ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true)})
    @ApiOperation(value = "Send OTP to email, this is for Change password post Request", notes = "Send OTP to email, this is for Change password post Request")
    @PostMapping("/change/password")
    public ResponseEntity<ResponsePojo> changePassword(PasswordPojo passwordPojo) {
        return null;
    }

    @ApiImplicitParams({@ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true)})
    @ApiOperation(value = "Change password post Request", notes = "Change password post Request")
    @PostMapping("/reset/password")
    public ResponseEntity<ResponsePojo> resetPassword(@RequestBody ResetPasswordPojo resetPasswordPojo) {
        return ResponseEntity.ok(passwordService.resetPassword(resetPasswordPojo));
    }

    @ApiOperation(value = "Forgot password post Request", notes = "Forgot password post Request")
    @PostMapping("/change/forgot/password")
    public ResponseEntity<ResponsePojo> changeForgotPassword(@RequestBody PasswordPojo passwordPojo) {
        return ResponseEntity.ok(passwordService.setForgotPassword(passwordPojo));
    }

    private String getBaseUrl(HttpServletRequest request) {
        return "http://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath();
    }

}
