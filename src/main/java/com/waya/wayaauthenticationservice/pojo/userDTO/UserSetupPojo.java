package com.waya.wayaauthenticationservice.pojo.userDTO;

import java.math.BigDecimal;

import javax.validation.constraints.NotNull;

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

	@NotNull(message = "UserId passed cannot be null")
    private Long userId;
    
    @NotNull(message = "Transaction Limit cannot be null")
    private BigDecimal transactionLimit;
    
}
