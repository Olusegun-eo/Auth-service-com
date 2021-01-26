package com.waya.wayaauthenticationservice.pojo;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PasswordPojo {
    private String oldPassword;
    private String newPassword;
    private String email;
}
