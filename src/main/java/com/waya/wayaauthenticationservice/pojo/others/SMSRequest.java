package com.waya.wayaauthenticationservice.pojo.others;

import com.waya.wayaauthenticationservice.util.ValidPhone;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class SMSRequest {

    @NotBlank(message = "Phone Number Cannot be blank")
    @ValidPhone
    private String phoneNumber;

}
