package com.waya.wayaauthenticationservice.controller;

import com.waya.wayaauthenticationservice.entity.OTPBase;
import com.waya.wayaauthenticationservice.enums.OTPRequestType;
import com.waya.wayaauthenticationservice.repository.OTPRepository;
import com.waya.wayaauthenticationservice.util.EnumValue;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Response Headers")})
    @GetMapping("/{id}")
    public OTPBase getOTPToken(@Email String email,
                               @EnumValue(enumClass = OTPRequestType.class, message = "Must be of type {enumClass}")
            String otpRequestType){


        return null;
    }
}
