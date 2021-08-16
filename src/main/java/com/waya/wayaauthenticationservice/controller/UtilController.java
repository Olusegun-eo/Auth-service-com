package com.waya.wayaauthenticationservice.controller;

import com.waya.wayaauthenticationservice.entity.OTPBase;
import com.waya.wayaauthenticationservice.enums.OTPRequestType;
import com.waya.wayaauthenticationservice.repository.OTPRepository;
import com.waya.wayaauthenticationservice.response.ApiResponseBody;
import com.waya.wayaauthenticationservice.util.EnumValue;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Email;

@CrossOrigin
@RestController
@RequestMapping("/api/v1/utils")
@Tag(name = "UTILITIES SERVICE", description = "Utilities Service API")
@Validated
@AllArgsConstructor
public class UtilController {

    private OTPRepository otpRepository;

    @ApiOperation(value = "Get Last Token by Email and Request Type (In-app use only)", tags = {"UTILITIES SERVICE"})
    @GetMapping("/otp-email/{email}/{requestType}")
    public ApiResponseBody<OTPBase> getOTPTokenByEmail(@PathVariable  @Email String email,
                                            @PathVariable @EnumValue(enumClass = OTPRequestType.class, message = "Must be of type {enumClass}")
            String requestType){
        OTPBase dbObject = otpRepository.findLastOTPByEmail(email, requestType).orElse(null);

        return new ApiResponseBody<>(dbObject, "Success", true);
    }

    @ApiOperation(value = "Get Last Token by PhoneNumber and Request Type (In-app use only)", tags = {"UTILITIES SERVICE"})
    @GetMapping("/otp-phone/{phoneNumber}/{requestType}")
    public ApiResponseBody<OTPBase> getOTPTokenByPhone(@PathVariable  @Email String phoneNumber,
                                            @PathVariable @EnumValue(enumClass = OTPRequestType.class, message = "Must be of type {enumClass}")
                                                    String requestType){
        OTPBase dbObject = otpRepository.findLastOTPByPhoneNumber(phoneNumber, requestType).orElse(null);

        return new ApiResponseBody<>(dbObject, "Success", true);
    }
}
