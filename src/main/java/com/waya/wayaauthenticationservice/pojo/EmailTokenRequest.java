package com.waya.wayaauthenticationservice.pojo;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Getter
@Setter
public class EmailTokenRequest {

    @Email(message = "please enter a valid email")
    private String email;

    @NotBlank(message = "make sure you entered the right key *fullName* and the value must not be null or blank")
    private String fullName;
}

