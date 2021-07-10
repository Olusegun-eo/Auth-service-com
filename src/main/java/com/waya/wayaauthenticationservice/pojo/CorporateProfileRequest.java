package com.waya.wayaauthenticationservice.pojo;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
public class CorporateProfileRequest {

    @NotBlank(message = "please enter your organisation name")
    private String organisationName;

    @NotBlank(message = "please enter your organisation type")
    private String organisationType;

    @NotBlank(message = "please enter your organisation type")
    private String organisationEmail;

    @NotBlank(message = "please enter your business type")
    private String businessType;

    @NotBlank(message = "please enter your organisation email")
    @Email(message = "please enter a valid email")
    private String email;

    @NotBlank(message = "please enter your organisation phone number")
    private String phoneNumber;

    @NotBlank(message = "please enter your firstName")
    private String firstName;

    @NotBlank(message = "please enter your surname")
    private String surname;

    @NotBlank(message = "please provide the userId")
    @NotNull(message = "make sure you entered the right key *userId* , and the value must not be null")
    private String userId;

    private String referralCode;
}
