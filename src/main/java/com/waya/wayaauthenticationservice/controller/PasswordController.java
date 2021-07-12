package com.waya.wayaauthenticationservice.controller;

import com.waya.wayaauthenticationservice.pojo.password.PasswordPojo;
import com.waya.wayaauthenticationservice.pojo.password.ResetPasswordPojo;
import com.waya.wayaauthenticationservice.response.ResponsePojo;
import com.waya.wayaauthenticationservice.service.PasswordService;
import com.waya.wayaauthenticationservice.util.SecurityConstants;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/v1/password")
@Tag(name = "Password", description = "Authentication Password Service API")
//@PreAuthorize(value = "hasRole('ADMIN')")
//@PreAuthorize(value = "@userSecurity.useHierarchy(#email, authentication)")
public class PasswordController {

    private PasswordService passwordService;

    public PasswordController(PasswordService passwordService) {
        this.passwordService = passwordService;
    }

    // @ApiImplicitParams({ @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
    @ApiOperation(value = "Send OTP to email, this is for forgot password post Request", notes = "Send OTP to email, this is for forgot password post Request")
    @GetMapping("/forgot/password/{email}")
    public ResponseEntity<ResponsePojo> forgotPassword(@PathVariable("email") String email) {
        return ResponseEntity.ok(passwordService.sendOtp(email));
    }

    @ApiImplicitParams({@ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true)})
    @ApiOperation(value = "Send OTP to email, this is for Change password post Request", notes = "Send OTP to email, this is for Change password post Request")
    @GetMapping("/change/password/{email}")
    public ResponseEntity<ResponsePojo> changePassword(@PathVariable("email") String email) {
        return ResponseEntity.ok(passwordService.sendOtp(email));
    }

    @ApiImplicitParams({@ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true)})
    @ApiOperation(value = "Change password post Request", notes = "Change password post Request")
    @PostMapping("/reset/password")
    public ResponseEntity<ResponsePojo> resetPassword(@RequestBody ResetPasswordPojo resetPasswordPojo) {
        return ResponseEntity.ok(passwordService.resetPassword(resetPasswordPojo));
    }

    // @ApiImplicitParams({ @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
    @ApiOperation(value = "Forgot password post Request", notes = "Forgot password post Request")
    @PostMapping("/change/forgot/password")
    public ResponseEntity<ResponsePojo> changeForgotPassword(@RequestBody PasswordPojo passwordPojo) {
        return ResponseEntity.ok(passwordService.setForgotPassword(passwordPojo));
    }
}
