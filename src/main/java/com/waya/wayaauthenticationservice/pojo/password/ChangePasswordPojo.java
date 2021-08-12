package com.waya.wayaauthenticationservice.pojo.password;

import javax.validation.constraints.NotBlank;

import com.waya.wayaauthenticationservice.util.CustomValidator;
import com.waya.wayaauthenticationservice.enums.Type;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangePasswordPojo {

    @NotBlank(message = "NewPassword cannot be blank")
    private String newPassword;

    @NotBlank(message = "phoneOrEmail Field cannot be blank or Null")
    @CustomValidator(message = "phoneOrEmail field has to be either a Phone or an Email", type = Type.EMAIL_OR_PHONE)
    private String phoneOrEmail;

    @NotBlank(message = "OldPassword cannot be blank")
    private String oldPassword;

    public ChangePasswordPojo(){}

    public void setPhoneOrEmail(String value) {
    	if(value.startsWith("+"))
    		value = value.substring(1);
		this.phoneOrEmail = value.replaceAll("\\s+", "").trim();
    }
}
