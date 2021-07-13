package com.waya.wayaauthenticationservice.pojo.others;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PasswordPojo2 {

    //private String newPassword;

    @NotBlank(message = "Email cannot be blank or null")
    @Email(message = "Email must be valid")
    private String email;
}
