package com.waya.wayaauthenticationservice.pojo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserPojo {
    private Long id;
    private String email;
    private Long phoneNumber;
    private String bvn;
    private String firstName;
    private String lastName;
    private String password;
}
