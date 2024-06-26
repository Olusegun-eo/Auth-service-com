package com.waya.wayaauthenticationservice.pojo.others;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValidateUserPojo {

    private String id;
    private String email;
    private String phoneNumber;
    private String referenceCode;
    private String firstName;
    private String surname;
    private String password;
    private boolean phoneVerified = false;
    private boolean emailVerified = false;
    private boolean admin = false;
    private boolean pinCreated = false;
    private boolean isCorporate = false;
    private Set<String> roles = new HashSet<>();
    private Set<String> permits = new HashSet<>();
    private String transactionLimit;
}
