package com.waya.wayaauthenticationservice.pojo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangePasswordPojo {
    private String oldPassword;
    private String newPassword;
    private Long userId;
}
