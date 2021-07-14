package com.waya.wayaauthenticationservice.pojo.others;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValidateUserPojo {

    private long id;
    private String email;
    private String phoneNumber;
    private String referenceCode;
    private String firstName;
    private String surname;
    private String password;
    private boolean phoneVerified = false;
    private boolean emailVerified = false;
    private boolean pinCreated = false;
    private boolean isCorporate = false;
    private List<String> roles;

}
