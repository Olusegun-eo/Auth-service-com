package com.waya.wayaauthenticationservice.pojo.others;

import lombok.Data;

@Data
public class VirtualAccountResponse {

    private Long id;
    private String bankName;
    private String bankCode;
    private String accountNumber;
    private String accountName;
    private String userId;
    private boolean deleted;
}
