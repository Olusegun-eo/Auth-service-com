package com.waya.wayaauthenticationservice.pojo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ProfilePojo {
    private String email;
    private String firstName;
    private String phoneNumber;
    private String surname;
    private String userId;
    private boolean corporate = true;
}
