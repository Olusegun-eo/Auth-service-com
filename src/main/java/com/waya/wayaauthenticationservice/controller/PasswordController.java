package com.waya.wayaauthenticationservice.controller;

import com.waya.wayaauthenticationservice.pojo.password.PasswordPojo;
import com.waya.wayaauthenticationservice.pojo.password.PinPojo;
import com.waya.wayaauthenticationservice.pojo.password.PinPojo2;
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
        return passwordService.sendPasswordResetOTP(email, redirectUrl);
    }

    @ApiOperation(value = "Forgot password post Request", notes = "Forgot password post Request")
    @PostMapping("/forgot-password")
    public ResponseEntity<?> changeForgotPassword(@Valid @RequestBody PasswordPojo passwordPojo) {
        return passwordService.setForgotPassword(passwordPojo);
    }

    @ApiOperation(value = "Pin Change (Service consumption only. Do not Use)", notes = "This is meant for service consumption", tags = {
            "AUTH"})
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Response Headers")})
    @PostMapping("/pin-change")
    public ResponseEntity<?> changePin(@RequestBody PinPojo2 pinPojo) {
        return passwordService.changePin(pinPojo);
    }

    @ApiOperation(value = "Forgot Pin (Service consumption only. Do not Use)", notes = "This is meant for service consumption", tags = {
            "AUTH"})
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Response Headers")})
    @PostMapping("/forgot-pin")
    public ResponseEntity<?> forgotPin(@RequestBody PinPojo pinPojo) {
        return passwordService.forgotPin(pinPojo);
    }

    @ApiOperation(value = "Pin Creation", notes = "This endpoint help user create transaction PIN", tags = {"AUTH"})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true)})
    @PostMapping("/create-pin")
    public ResponseEntity<?> createPin(@RequestBody PinPojo pinPojo) {
        return passwordService.createPin(pinPojo);
    }

    @ApiOperation(value = "PIN verification (Service consumption only. Do not Use)", notes = "This endpoint help validate user by Pin and is meant for service consumption only", tags = {
            "AUTH"})
    @GetMapping("/validate-pin/{userId}/{pin}")
    public ResponseEntity<?> validateUserByPin(@PathVariable Long userId, @PathVariable int pin) {
        return passwordService.validatePin(userId, pin);
    }

    @ApiOperation(value = "PIN verification for user consumption", notes = "This endpoint help validate user by Pin by Authorisation token", tags = {
            "AUTH"})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true)})
    @GetMapping("/validate-pin/{pin}")
    public ResponseEntity<?> validateUserByPin(@PathVariable int pin) {
        return passwordService.validatePinFromUser(pin);
    }

    private String getBaseUrl(HttpServletRequest request) {
        return "http://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath();
    }

}
