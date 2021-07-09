package com.waya.wayaauthenticationservice.pojo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import java.util.UUID;

@Getter
@Setter
public class UpdateCorporateProfileRequest {

    @JsonIgnore
    private UUID id;

    @NotBlank(message = "please enter your organisation name")
    private String organisationName;

    @NotBlank(message = "please enter your organisation type")
    private String organisationType;

    @NotBlank(message = "please enter your business type")
    private String businessType;

    @NotBlank(message = "please enter your email")
    @Email(message = "please enter a valid email")
    @Column(unique = true)
    private String organisationEmail;

    @NotBlank(message = "please enter your phone number")
    private String phoneNumber;

    @NotBlank(message = "please enter your surname")
    private String surname;

    @NotBlank(message = "please enter your firstname")
    private String firstName;

    private String state;

    private String city;

    private String officeAddress;

    private String gender;
}
