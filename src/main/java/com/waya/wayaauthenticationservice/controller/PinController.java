package com.waya.wayaauthenticationservice.controller;

import com.waya.wayaauthenticationservice.pojo.password.ChangePINPojo;
import com.waya.wayaauthenticationservice.pojo.password.ForgotPINPojo;
import com.waya.wayaauthenticationservice.pojo.password.NewPinPojo;
import com.waya.wayaauthenticationservice.service.PasswordService;
import com.waya.wayaauthenticationservice.util.ValidPhone;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Email;

@RestController
@RequestMapping("/api/v1/pin")
@Tag(name = "PIN RESOURCE", description = "REST API for PIN Service API")
@CrossOrigin
@Validated
public class PinController {

    @Autowired
    private PasswordService passwordService;

    @ApiOperation(value = "Pin Creation", notes = "This endpoint help user create transaction PIN", tags = {"PIN RESOURCE"})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true)})
    @PostMapping("/create-pin")
    public ResponseEntity<?> createPin(@Valid @RequestBody NewPinPojo pinPojo) {
        return passwordService.createPin(pinPojo);
    }

    @ApiOperation(value = "PIN verification (Service consumption only. Do not Use)", notes = "This endpoint help validate user by Pin and is meant for service consumption only", tags = {
            "PIN RESOURCE"})
    @GetMapping("/validate-pin/{userId}/{pin}")
    public ResponseEntity<?> validateUserByPin(@PathVariable Long userId, @PathVariable int pin) {
        return passwordService.validatePin(userId, pin);
    }

    @ApiOperation(value = "PIN verification for user consumption", notes = "This endpoint help validate user by Pin by Authorisation token", tags = {
            "PIN RESOURCE"})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true)})
    @GetMapping("/validate-pin/{pin}")
    public ResponseEntity<?> validateUserByPin(@PathVariable int pin) {
        return passwordService.validatePinFromUser(pin);
    }

    @ApiImplicitParams({@ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true)})
    @ApiOperation(value = "Send OTP to email, this is for forgot pin Post Request", tags = {"PIN RESOURCE"}, notes = "Send OTP to email, this is for forgot pin post Request")
    @GetMapping("/forgot-pin/byEmail")
    @CrossOrigin
    public ResponseEntity<?> forgotPinRequestEmail(@RequestParam("email") @Email String email, @RequestParam("redirectUrl") String redirectUrl) {
        return passwordService.sendPinResetOTPByEmail(email, redirectUrl);
    }

    @ApiImplicitParams({@ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true)})
    @ApiOperation(value = "Send OTP to Phone, this is for forgot pin post Request", tags = {"PIN RESOURCE"}, notes = "Send OTP to PhoneNumber, this is for forgot pin post Request")
    @GetMapping("/forgot-pin/byPhone")
    @CrossOrigin
    public ResponseEntity<?> forgotPinRequestPhone(@RequestParam("phoneNumber") @ValidPhone String phoneNumber) {
        return passwordService.sendResetOTPByPhoneNumber(phoneNumber);
    }

    @ApiImplicitParams({@ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true)})
    @ApiOperation(value = "Forgot pin post Request", tags = {"PIN RESOURCE"}, notes = "Forgot pin post Request")
    @PostMapping("/forgot-pin")
    @CrossOrigin
    public ResponseEntity<?> changeForgotPin(@Valid @RequestBody ForgotPINPojo changePinPojo) {
        return passwordService.changeForgotPIN(changePinPojo);
    }

    @ApiImplicitParams({@ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true)})
    @ApiOperation(value = "Send OTP to email, this is for Change pin post Request", tags = {"PIN RESOURCE"}, notes = "Send OTP to email, this is for Change pin post Request")
    @GetMapping("/change-pin/byEmail")
    @CrossOrigin
    public ResponseEntity<?> requestChangePinEmail(@RequestParam("email") @Email String email, @RequestParam("redirectUrl") String redirectUrl) {
        return passwordService.sendPinResetOTPByEmail(email, redirectUrl);
    }

    @ApiImplicitParams({@ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true)})
    @ApiOperation(value = "Send OTP to email, this is for Change pin post Request", tags = {"PIN RESOURCE"}, notes = "Send OTP to email, this is for Change pin post Request")
    @GetMapping("/change-pin/byPhone")
    @CrossOrigin
    public ResponseEntity<?> requestChangePinPhone(@RequestParam("phoneNumber") @ValidPhone String phoneNumber) {
        return passwordService.sendResetOTPByPhoneNumber(phoneNumber);
    }

    @ApiImplicitParams({@ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true)})
    @ApiOperation(value = "Change pin post Request", tags = {"PIN RESOURCE"}, notes = "Change pin post Request")
    @PostMapping("/change-pin")
    @CrossOrigin
    public ResponseEntity<?> changePin(@Valid @RequestBody ChangePINPojo pinPojo) {
        return passwordService.changePin(pinPojo);
    }

}
