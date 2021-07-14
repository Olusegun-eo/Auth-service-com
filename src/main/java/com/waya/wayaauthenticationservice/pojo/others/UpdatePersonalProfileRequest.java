package com.waya.wayaauthenticationservice.pojo.others;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import java.util.UUID;

@Getter
@Setter
public class UpdatePersonalProfileRequest {

    @JsonIgnore
    private UUID id;

    @NotBlank(message = "please provide your email")
    @Email(message = "please enter a valid email")
    private String email;

    @NotBlank(message = "please provide your first name")
    private String firstName;

    @NotBlank(message = "please provide your surname")
    private String surname;

    @NotBlank(message = "please provide your phone number")
    private String phoneNumber;

    private String middleName;

    private String dateOfBirth;

    private String gender;

    private String district;

    private String address;
}

