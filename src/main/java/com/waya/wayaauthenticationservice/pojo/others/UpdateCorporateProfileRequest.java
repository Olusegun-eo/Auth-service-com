package com.waya.wayaauthenticationservice.pojo.others;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Getter
@Setter
public class UpdateCorporateProfileRequest extends UpdatePersonalProfileRequest {

    @NotBlank(message = "please enter your organisation name")
    private String organisationName;

    private String organisationPhone;
    private String organizationCity;
    private String organizationState;

    @NotBlank(message = "please enter your organisation type")
    private String organisationType;

    @NotBlank(message = "please enter your business type")
    private String businessType;

    @Email(message = "please enter a valid Organization email")
    private String organisationEmail;

    private String officeAddress;

}
