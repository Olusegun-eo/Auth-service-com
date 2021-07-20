package com.waya.wayaauthenticationservice.pojo.others;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.waya.wayaauthenticationservice.enums.Type;
import com.waya.wayaauthenticationservice.util.CustomValidator;
import com.waya.wayaauthenticationservice.util.ValidPhone;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import java.util.UUID;

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
