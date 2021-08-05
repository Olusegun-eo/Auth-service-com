package com.waya.wayaauthenticationservice.pojo.others;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WalletAccessPojo {

    private boolean acctClosed = false;
    private boolean acctfreez = false;
    private boolean amountRestrict = false;
    private String customerAccountNumber;
    private String freezCode = "";
    private String freezReason = "";
    private BigDecimal lienAmount;
    private String lienReason = "";
}
