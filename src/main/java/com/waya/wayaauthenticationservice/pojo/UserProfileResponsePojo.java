package com.waya.wayaauthenticationservice.pojo;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class UserProfileResponsePojo {
    private Long id;
    private String email;
    private boolean isEmailVerified;
    private String phoneNumber;
    private String firstName;
    private String lastName;
}
