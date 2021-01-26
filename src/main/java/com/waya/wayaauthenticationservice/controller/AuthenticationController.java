package com.waya.wayaauthenticationservice.controller;

import com.waya.wayaauthenticationservice.pojo.*;
import com.waya.wayaauthenticationservice.service.impl.AuthenticationServiceImpl;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {

    @Autowired
    private AuthenticationServiceImpl authenticationServiceImpl;

    @ApiOperation("User Registration")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Response Headers")})
    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody UserPojo user) {
        return authenticationServiceImpl.createUser(user);
    }


    @ApiOperation("Verify OTP")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Response Headers")})
    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOTP(@RequestBody OTPPojo otpPojo) {
        return authenticationServiceImpl.verifyOTP(otpPojo);
    }

    @ApiOperation(value = "Password Change (Service consumption only. Do not Usr)", notes = "This is meant for service consumption")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Response Headers")})
    @PostMapping("/password-change")
    public ResponseEntity<?> changePass(@RequestBody PasswordPojo passwordPojo) {
        return authenticationServiceImpl.changePassword(passwordPojo);
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


}
