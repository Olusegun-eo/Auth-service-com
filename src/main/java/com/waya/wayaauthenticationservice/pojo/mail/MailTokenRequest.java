package com.waya.wayaauthenticationservice.pojo.mail;

import com.waya.wayaauthenticationservice.util.CustomValidator;
import com.waya.wayaauthenticationservice.enums.Type;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Data
public class MailTokenRequest {

    @NotBlank(message = "name cannot be blank")
    private String name;

    @NotBlank(message = "Message cannot be blank")
    @CustomValidator( type = Type.CONTAINS, message = "message must contain the keyword: placeholder {2}", values = {"placeholder"})
    private String message;

    @NotBlank(message = "name cannot be blank")
    @Email
    private String email;
}
