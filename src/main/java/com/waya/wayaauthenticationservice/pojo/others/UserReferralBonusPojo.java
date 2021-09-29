package com.waya.wayaauthenticationservice.pojo.others;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class UserReferralBonusPojo {
    @NotEmpty
    @NotNull(message = "userId must be provided")
    private String userId;

    @ApiModelProperty(example = "10.00")
    @NotNull(message = "value is required ")
    @DecimalMin(value = "0.00", message = "Minimum bonus value is 0.00")
    private BigDecimal amount;
}
