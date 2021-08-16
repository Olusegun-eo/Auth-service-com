package com.waya.wayaauthenticationservice.pojo.userDTO;

import java.math.BigDecimal;

import javax.validation.constraints.NotNull;

import com.waya.wayaauthenticationservice.enums.Type;
import com.waya.wayaauthenticationservice.util.CustomValidator;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserSetupPojo {
	
	private long id;

    @CustomValidator(message = "UserId Passed must be numeric", type = Type.NUMERIC_STRING)
    private String userId;
    
    @NotNull(message = "Transaction Limit cannot be null")
    private BigDecimal transactionLimit;
    
}
