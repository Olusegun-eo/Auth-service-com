package com.waya.wayaauthenticationservice.pojo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserPojo {
    private String email;
    private Long phoneNumber;
    private String referenceCode;
    private String firstName;
    private String surname;
    private String password;
    private boolean isCorporate = false;
}
