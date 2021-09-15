package com.waya.wayaauthenticationservice.pojo.others;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class BonusTransferPojo {
    @ApiModelProperty(example = "50")
    @NotNull(message = "amount is required")
    @DecimalMin(value = "0.01", message = "minimum amount required is 0.01")
    private BigDecimal amount;
    @NotBlank(message = "please enter your customerAccountNumber")
    private String customerAccountNumber;
    @NotBlank(message = "please enter your eventId")
    private String eventId;
    private String paymentReference;
    private String tranCrncy;
    private String tranNarration;

}
