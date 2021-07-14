package com.waya.wayaauthenticationservice.controller;

import com.waya.wayaauthenticationservice.pojo.password.ChangePINPojo;
import com.waya.wayaauthenticationservice.pojo.password.NewPinPojo;
import com.waya.wayaauthenticationservice.service.PasswordService;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Email;

@RestController
@RequestMapping("/api/v1/pin")
@Api(tags = {"PIN Resource"})
@SwaggerDefinition(tags = {
        @Tag(name = "PIN Resource", description = "REST API for PIN Service.")
})
@CrossOrigin
public class PinController {

    @Autowired
    private PasswordService passwordService;

    @ApiOperation(value = "Pin Creation", notes = "This endpoint help user create transaction PIN", tags = {"PIN Resource"})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true)})
    @PostMapping("/create-pin")
    public ResponseEntity<?> createPin(@Valid @RequestBody NewPinPojo pinPojo) {
        return passwordService.createPin(pinPojo);
    }

    @ApiOperation(value = "PIN verification (Service consumption only. Do not Use)", notes = "This endpoint help validate user by Pin and is meant for service consumption only", tags = {
            "PIN Resource"})
    @GetMapping("/validate-pin/{userId}/{pin}")
    public ResponseEntity<?> validateUserByPin(@PathVariable Long userId, @PathVariable int pin) {
        return passwordService.validatePin(userId, pin);
    }

    @ApiOperation(value = "PIN verification for user consumption", notes = "This endpoint help validate user by Pin by Authorisation token", tags = {
            "PIN Resource"})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true)})
    @GetMapping("/validate-pin/{pin}")
    public ResponseEntity<?> validateUserByPin(@PathVariable int pin) {
        return passwordService.validatePinFromUser(pin);
    }

    @ApiImplicitParams({@ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true)})
    @ApiOperation(value = "Send OTP to email, this is for forgot pin Post Request", notes = "Send OTP to email, this is for forgot pin post Request")
    @GetMapping("/forgot-pin/{email}/{redirectUrl}")
    @CrossOrigin
    public ResponseEntity<?> forgotPinRequestEmail(@Valid @Email @PathVariable("email") String email, @PathVariable("redirectUrl") String redirectUrl) {
        return passwordService.sendPinResetOTPByEmail(email, redirectUrl);
    }

    @ApiImplicitParams({@ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true)})
    @ApiOperation(value = "Send OTP to Phone, this is for forgot pin post Request", notes = "Send OTP to PhoneNumber, this is for forgot pin post Request")
    @GetMapping("/forgot-pin/{phoneNumber}")
    @CrossOrigin
    public ResponseEntity<?> forgotPinRequestPhone(@PathVariable("phoneNumber") String phoneNumber) {
        return passwordService.sendResetOTPByPhoneNumber(phoneNumber);
    }

    @ApiImplicitParams({@ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true)})
    @ApiOperation(value = "Forgot pin post Request", notes = "Forgot pin post Request")
    @PostMapping("/forgot-pin")
    @CrossOrigin
    public ResponseEntity<?> changeForgotPin(@Valid @RequestBody NewPinPojo changePinPojo) {
        return passwordService.changeForgotPIN(changePinPojo);
    }

    @ApiImplicitParams({@ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true)})
    @ApiOperation(value = "Send OTP to email, this is for Change pin post Request", notes = "Send OTP to email, this is for Change pin post Request")
    @GetMapping("/change-pin/{email}/{redirectUrl}")
    @CrossOrigin
    public ResponseEntity<?> requestChangePinEmail(@Valid @Email @PathVariable("email") String email, @PathVariable("redirectUrl") String redirectUrl) {
        return passwordService.sendPinResetOTPByEmail(email, redirectUrl);
    }

    @ApiImplicitParams({@ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true)})
    @ApiOperation(value = "Send OTP to email, this is for Change pin post Request", notes = "Send OTP to email, this is for Change pin post Request")
    @GetMapping("/change-pin/{phoneNumber}")
    @CrossOrigin
    public ResponseEntity<?> requestChangePinPhone(@PathVariable("phoneNumber") String phoneNumber) {
        return passwordService.sendResetOTPByPhoneNumber(phoneNumber);
    }

    @ApiImplicitParams({@ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true)})
    @ApiOperation(value = "Change pin post Request", notes = "Change pin post Request")
    @PostMapping("/change-pin")
    @CrossOrigin
    public ResponseEntity<?> changePin(@Valid @RequestBody ChangePINPojo pinPojo) {
        return passwordService.changePin(pinPojo);
    }

}
