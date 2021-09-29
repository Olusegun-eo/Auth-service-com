package com.waya.wayaauthenticationservice.controller;

import com.waya.wayaauthenticationservice.pojo.password.ChangePINPojo;
import com.waya.wayaauthenticationservice.pojo.password.NewPinPojo;
import com.waya.wayaauthenticationservice.service.PasswordService;
import com.waya.wayaauthenticationservice.util.ValidPhone;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

    @ApiOperation(value = "${api.pin.create-pin-by-email.description}",
            notes = "${api.pin.create-pin-by-email.notes}", tags = {"PIN RESOURCE"})
    @GetMapping("/create-pin/byEmail")
    @PreAuthorize(value = "@userSecurity.useHierarchy(#email, authentication)")
    public ResponseEntity<?> sendPinCreationOTPEmail(@RequestParam("email") @Email String email,
                                                        @RequestParam(name = "redirectUrl", required = false) String redirectUrl) {
        return passwordService.sendPinCreationOTPEmail(email, redirectUrl);
    }

    @ApiOperation(value = "${api.pin.create-pin-by-phone.description}",
            notes = "${api.pin.create-pin-by-phone.notes}", tags = {"PIN RESOURCE"})
    @GetMapping("/create-pin/byPhone")
    @PreAuthorize(value = "@userSecurity.useHierarchy(#phoneNumber, authentication)")
    public ResponseEntity<?> sendPinCreationOTPPhone(@RequestParam("phoneNumber") @ValidPhone String phoneNumber) {
        return passwordService.sendPinCreationOTPPhone(phoneNumber);
    }

    @ApiOperation(value = "${api.pin.create-pin.description}",
            notes = "${api.pin.create-pin.notes}", tags = {"PIN RESOURCE"})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", dataTypeClass = String.class, value = "token", paramType = "header", required = true)})
    @PostMapping("/create-pin")
    @PreAuthorize(value = "@userSecurity.useHierarchy(#pinPojo.phoneOrEmail, authentication)")
    public ResponseEntity<?> createPin(@Valid @RequestBody NewPinPojo pinPojo) {
        return passwordService.createPin(pinPojo);
    }

    @ApiOperation(value = "${api.pin.validate-pin-by-userId.description}",
            notes = "${api.pin.validate-pin-by-userId.notes}", tags = {"PIN RESOURCE"})
    @GetMapping("/validate-pin/{userId}/{pin}")
    @PreAuthorize(value = "@userSecurity.useHierarchy(#userId, authentication)")
    public ResponseEntity<?> validateUserByPin(@PathVariable Long userId, @PathVariable int pin) {
        return passwordService.validatePin(userId, pin);
    }

    @ApiOperation(value = "${api.pin.validate-pin.description}",
            notes = "${api.pin.validate-pin.notes}", tags = {"PIN RESOURCE"})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", dataTypeClass = String.class, value = "token", paramType = "header", required = true)})
    @GetMapping("/validate-pin/{pin}")
    public ResponseEntity<?> validateUserByPin(@PathVariable int pin) {
        return passwordService.validatePinFromUser(pin);
    }

    @ApiImplicitParams({@ApiImplicitParam(name = "authorization", dataTypeClass = String.class, value = "token", paramType = "header", required = true)})
    @ApiOperation(value = "${api.pin.forgot-pin-by-email.description}",
            notes = "${api.pin.forgot-pin-by-email.notes}", tags = {"PIN RESOURCE"})
    @GetMapping("/forgot-pin/byEmail")
    @CrossOrigin
    @PreAuthorize(value = "@userSecurity.useHierarchy(#email, authentication)")
    public ResponseEntity<?> forgotPinRequestEmail(@RequestParam("email") @Email String email,
                                                   @RequestParam(name = "redirectUrl", required = false) String redirectUrl) {
        return passwordService.sendPinResetOTPByEmail(email, redirectUrl);
    }

    @ApiImplicitParams({@ApiImplicitParam(name = "authorization", dataTypeClass = String.class, value = "token", paramType = "header", required = true)})
    @ApiOperation(value = "${api.pin.forgot-pin-by-phone.description}",
            notes = "${api.pin.forgot-pin-by-phone.notes}", tags = {"PIN RESOURCE"})
    @GetMapping("/forgot-pin/byPhone")
    @CrossOrigin
    @PreAuthorize(value = "@userSecurity.useHierarchy(#phoneNumber, authentication)")
    public ResponseEntity<?> forgotPinRequestPhone(@RequestParam("phoneNumber") @ValidPhone String phoneNumber) {
        return passwordService.sendPINResetOTPByPhoneNumber(phoneNumber);
    }

    @ApiImplicitParams({@ApiImplicitParam(name = "authorization", dataTypeClass = String.class, value = "token", paramType = "header", required = true)})
    @ApiOperation(value = "${api.pin.forgot-pin.description}",
            notes = "${api.pin.forgot-pin.notes}", tags = {"PIN RESOURCE"})
    @PostMapping("/forgot-pin")
    @CrossOrigin
    @PreAuthorize(value = "@userSecurity.useHierarchy(#pojo.phoneOrEmail, authentication)")
    public ResponseEntity<?> changeForgotPin(@Valid @RequestBody NewPinPojo pojo) {
        return passwordService.changeForgotPIN(pojo);
    }

    @ApiImplicitParams({@ApiImplicitParam(name = "authorization", dataTypeClass = String.class, value = "token", paramType = "header", required = true)})
    @ApiOperation(value = "${api.pin.change-pin-by-email.description}",
            notes = "${api.pin.change-pin-by-email.notes}", tags = {"PIN RESOURCE"})
    @GetMapping("/change-pin/byEmail")
    @CrossOrigin
    @PreAuthorize(value = "@userSecurity.useHierarchy(#email, authentication)")
    public ResponseEntity<?> requestChangePinEmail(@RequestParam("email") @Email String email,
                                                   @RequestParam(name = "redirectUrl", required = false) String redirectUrl) {
        return passwordService.sendPinChangeOTPByEmail(email, redirectUrl);
    }

    @ApiImplicitParams({@ApiImplicitParam(name = "authorization", dataTypeClass = String.class, value = "token", paramType = "header", required = true)})
    @ApiOperation(value = "${api.pin.change-pin-by-phone.description}",
            notes = "${api.pin.change-pin-by-phone.notes}", tags = {"PIN RESOURCE"})
    @GetMapping("/change-pin/byPhone")
    @CrossOrigin
    @PreAuthorize(value = "@userSecurity.useHierarchy(#phoneNumber, authentication)")
    public ResponseEntity<?> requestChangePinPhone(@RequestParam("phoneNumber") @ValidPhone String phoneNumber) {
        return passwordService.sendPINChangeOTPByPhoneNumber(phoneNumber);
    }

    @ApiImplicitParams({@ApiImplicitParam(name = "authorization", dataTypeClass = String.class, value = "token", paramType = "header", required = true)})
    @ApiOperation(value = "${api.pin.change-pin.description}",
            notes = "${api.pin.change-pin.notes}", tags = {"PIN RESOURCE"})
    @PostMapping("/change-pin")
    @CrossOrigin
    @PreAuthorize(value = "@userSecurity.useHierarchy(#pinPojo.phoneOrEmail, authentication)")
    public ResponseEntity<?> changePin(@Valid @RequestBody ChangePINPojo pinPojo) {
        return passwordService.changePin(pinPojo);
    }

}
