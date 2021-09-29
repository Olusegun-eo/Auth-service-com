package com.waya.wayaauthenticationservice.pojo.others;


import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class ReferralBonusRequest {
    @ApiModelProperty(example = "1")
    private Long id;

    @ApiModelProperty(example = "10.00")
    @NotNull(message = "value is required ")
    @DecimalMin(value = "0.00", message = "Minimum bonus value is 0.00")
    private BigDecimal amount;

    @ApiModelProperty(example = "5: means 5 Transactions")
    @NotNull(message = "value is required ")
    private Integer numberOfTransaction;




}
