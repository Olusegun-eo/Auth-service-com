package com.waya.wayaauthenticationservice.pojo;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PinPojo {
    private Long userId;
    
    @Min(value = 1000, message = "Pin should not be less than 1000")
    @Max(value = 9999, message = "Pin should not be greater than 9999")
    private int pin;
    
    @NotNull(message = "Loan Type cannot be null")
    private String email;
}
