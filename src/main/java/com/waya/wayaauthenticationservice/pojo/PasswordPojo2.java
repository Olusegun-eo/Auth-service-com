package com.waya.wayaauthenticationservice.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PasswordPojo2 {
    private Long userId;
    private String newPassword;
    private String email;
}