package com.waya.wayaauthenticationservice.pojo.others;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.waya.wayaauthenticationservice.enums.Type;
import com.waya.wayaauthenticationservice.util.CustomValidator;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateWayagram {

    @NotBlank(message = "please provide the userId")
    @CustomValidator(type= Type.NUMERIC_STRING, message = "userId Passed must be Numeric")
    @JsonProperty("user_id")
    private String user_id;

    @NotBlank(message = "please enter your userName")
    @CustomValidator(type= Type.TEXT_STRING, message = "userName Passed must be Valid and not contain numerals")
    private String username;

    private boolean notPublic = false;

    @NotBlank(message = "please enter your displayName")
    @CustomValidator(type= Type.TEXT_STRING, message = "displayName Passed must be Valid and not contain numerals")
    private String displayName;
}
