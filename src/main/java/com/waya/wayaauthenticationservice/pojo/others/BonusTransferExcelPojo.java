package com.waya.wayaauthenticationservice.pojo.others;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BonusTransferExcelPojo {
    @ApiModelProperty(example = "50")
    @NotNull(message = "amount is required")
    @DecimalMin(value = "0.01", message = "minimum amount required is 0.01")
    private Double amount;
    @NotBlank(message = "please enter your customerAccountNumber")
    private String benefAccountNumber;
    private String debitAccountNumber;
    private String tranCrncy;
    private String tranNarration;

}

