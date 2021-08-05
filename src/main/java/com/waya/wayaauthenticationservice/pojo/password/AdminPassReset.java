package com.waya.wayaauthenticationservice.pojo.password;

import com.waya.wayaauthenticationservice.enums.Type;
import com.waya.wayaauthenticationservice.util.CustomValidator;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class AdminPassReset {

    @NotBlank(message = "NewPassword cannot be blank")
    private String newPassword;

    @NotBlank(message = "Field cannot be blank or Null")
    @CustomValidator(message = "phoneOrEmail field has to be either a Phone or an Email", type = Type.EMAIL_OR_PHONE)
    private String phoneOrEmail;
}
