package com.waya.wayaauthenticationservice.pojo.others;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.waya.wayaauthenticationservice.enums.Type;
import com.waya.wayaauthenticationservice.util.CustomValidator;
import com.waya.wayaauthenticationservice.util.ValidPhone;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class PersonalProfileRequest {

    @NotBlank(message = "please provide your email")
    @Email(message = "please enter a valid email")
    private String email;

    @NotBlank(message = "please provide your first name")
    @CustomValidator(type= Type.TEXT_STRING, message = "FirstName Passed must be Valid and not contain numerals")
    private String firstName;

    @NotBlank(message = "please provide your surname")
    @CustomValidator(type= Type.TEXT_STRING, message = "SurName Passed must be Valid and not contain numerals")
    private String surname;

    @NotBlank(message = "please provide your phone number")
    @ValidPhone
    private String phoneNumber;

    @NotBlank(message = "please provide the userId")
    @CustomValidator(type= Type.NUMERIC_STRING, message = "userId Passed must be Numeric")
    private String userId;

    private String referralCode;
}
