package com.waya.wayaauthenticationservice.pojo.others;

import com.waya.wayaauthenticationservice.enums.Type;
import com.waya.wayaauthenticationservice.util.CustomValidator;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class VirtualAccountPojo {

    @NotBlank(message = "Account Name Cannot be Blank")
    @CustomValidator(type= Type.TEXT_STRING, message = "Account Name Passed must be Valid and not contain numerals")
    private String accountName;

    @NotBlank(message = "please provide the userId")
    @CustomValidator(type= Type.NUMERIC_STRING, message = "userId Passed must be Numeric")
    private String userId;
}
