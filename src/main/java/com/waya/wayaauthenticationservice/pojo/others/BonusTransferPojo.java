package com.waya.wayaauthenticationservice.pojo.others;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class BonusTransferPojo {
    private BigDecimal amount;
    private String customerAccountNumber;
    private String eventId;
    private String paymentReference;
    private String tranCrncy;
    private String tranNarration;

}
