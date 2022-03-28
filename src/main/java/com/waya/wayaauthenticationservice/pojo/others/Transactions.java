package com.waya.wayaauthenticationservice.pojo.others;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.waya.wayaauthenticationservice.util.ThirdPartyNames;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class Transactions {
    private String transactionId;
    private ThirdPartyNames thirdPartyName;
    private BigDecimal amount;
    private Boolean successful;
    private String category;
    private String referralCode;
    private String biller;
    private String paymentRequest;
    private String paymentResponse;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+1")
    private Date transactionDateTime;

}
