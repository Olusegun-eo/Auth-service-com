package com.waya.wayaauthenticationservice.pojo.others;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class UserTransferToDefaultWallet {
    private BigDecimal amount;
    private String benefAccountNumber;
    private String debitAccountNumber;
    private String paymentReference;
    private String tranCrncy;
    private String tranNarration;
    private String tranType;
    private Long userId;

}
