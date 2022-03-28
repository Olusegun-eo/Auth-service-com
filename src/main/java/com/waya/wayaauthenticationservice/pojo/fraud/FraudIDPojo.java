package com.waya.wayaauthenticationservice.pojo.fraud;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FraudIDPojo {

    @NotNull(message = "UserId cannot be null")
    @Min(value = 1L, message = "UserId Passed must be positive")
    private Long userId;

    @Min(value = 1L, message = "Agent/Admin ID Passed must be positive")
    private Long adminId;
}
