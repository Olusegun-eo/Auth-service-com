package com.waya.wayaauthenticationservice.pojo.others;

import com.waya.wayaauthenticationservice.enums.Type;
import com.waya.wayaauthenticationservice.util.CustomValidator;
import com.waya.wayaauthenticationservice.util.ValidPhone;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Getter
@Setter
public class CorporateProfileRequest {

    @NotBlank(message = "please enter your organisation name")
    private String organisationName;

    @NotBlank(message = "please enter your organisation type")
    private String organisationType;

    private String organisationEmail;

    @NotBlank(message = "please enter your business type")
    private String businessType;

    @NotBlank(message = "please enter your organisation email")
    @Email(message = "please enter a valid email")
    private String email;

    @NotBlank(message = "please enter your phone number")
    @ValidPhone
    private String phoneNumber;

    private String dateOfBirth;

    private String gender;

    @NotBlank(message = "please enter your firstName")
    @CustomValidator(type= Type.TEXT_STRING, message = "FirstName Passed must be Valid and not contain numerals")
    private String firstName;

    @NotBlank(message = "please enter your surname")
    @CustomValidator(type= Type.TEXT_STRING, message = "SurName Passed must be Valid and not contain numerals")
    private String surname;

    @NotBlank(message = "please provide the userId")
    @CustomValidator(type= Type.NUMERIC_STRING, message = "userId Passed must be Numeric")
    private String userId;

    private String referralCode;
}
