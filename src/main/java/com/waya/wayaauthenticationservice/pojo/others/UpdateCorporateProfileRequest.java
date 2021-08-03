package com.waya.wayaauthenticationservice.pojo.others;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateCorporateProfileRequest extends UpdatePersonalProfileRequest {

    @NotBlank(message = "please enter your organisation name")
    private String organisationName;

    @NotBlank(message = "please enter your organisation type")
    private String organisationType;

    @NotBlank(message = "please enter your business type")
    private String businessType;

    @Email(message = "please enter a valid Organization email")
    private String organisationEmail;

    private String state;

    private String city;

    private String officeAddress;

}
