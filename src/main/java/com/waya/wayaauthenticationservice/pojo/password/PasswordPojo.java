package com.waya.wayaauthenticationservice.pojo.password;

import javax.validation.constraints.NotBlank;

import com.waya.wayaauthenticationservice.util.CustomValidator;
import com.waya.wayaauthenticationservice.enums.Type;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PasswordPojo {

    @NotBlank(message = "otp cannot be blank")
    @CustomValidator(message = "OTP Has to contain Numeric characters only", type = Type.NUMERIC_STRING)
    private String otp;

    @NotBlank(message = "phoneOrEmail Field cannot be blank or Null")
    @CustomValidator(message = "phoneOrEmail field has to be either a Phone or an Email", type = Type.EMAIL_OR_PHONE)
    private String phoneOrEmail;

    @NotBlank(message = "OldPassword cannot be blank")
    private String oldPassword;
    
    @NotBlank(message = "NewPassword cannot be blank")
    private String newPassword;

    public void setPhoneOrEmail(String value) {
    	if(value.startsWith("+"))
    		value = value.substring(1);
		this.phoneOrEmail = value.replaceAll("\\s+", "").trim();
    }
    
    public PasswordPojo(){}

}
