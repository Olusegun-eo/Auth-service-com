package com.waya.wayaauthenticationservice.pojo;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Getter
@Setter
public class PersonalProfileRequest {

    @NotBlank(message = "please provide your email")
    @Email(message = "please enter a valid email")
    private String email;

    @NotBlank(message = "please provide your first name")
    private String firstName;

    @NotBlank(message = "please provide your surname")
    private String surname;

    @NotBlank(message = "please provide your phone number")
    private String phoneNumber;

    @NotBlank(message = "please provide the userId")
    private String userId;

    private String referralCode;
}
