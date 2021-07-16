package com.waya.wayaauthenticationservice.pojo.others;

import com.waya.wayaauthenticationservice.util.CustomValidator;
import com.waya.wayaauthenticationservice.util.Type;
import com.waya.wayaauthenticationservice.util.ValidPhone;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class ToggleSMSRequest {
    @NotBlank(message = "Phone Number Cannot be blank")
    @ValidPhone
    @CustomValidator(message = "Phone Number must be 13 characters", type = Type.SIZE, min = 13, max = 13)
    private String phoneNumber;

}
