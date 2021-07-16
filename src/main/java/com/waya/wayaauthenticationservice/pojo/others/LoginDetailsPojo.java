package com.waya.wayaauthenticationservice.pojo.others;

import com.waya.wayaauthenticationservice.util.CustomValidator;
import com.waya.wayaauthenticationservice.enums.Type;
import lombok.Data;

@Data
public class LoginDetailsPojo {

    //private boolean admin = false;
    @CustomValidator(message = "Must be either a valid email or phoneNumber", type = Type.EMAIL_OR_PHONE)
    private String emailOrPhoneNumber;
    private String password;
}
