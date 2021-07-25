package com.waya.wayaauthenticationservice.pojo.others;

import lombok.Data;

@Data
public class VirtualAccountResponse {

    private Integer id;
    private String bankName;
    private String bankCode;
    private String accountNumber;
    private String accountName;
    private String userId;
    private Boolean deleted;
}
