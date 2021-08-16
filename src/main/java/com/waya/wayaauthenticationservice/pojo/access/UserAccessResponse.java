package com.waya.wayaauthenticationservice.pojo.access;

import java.math.BigDecimal;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class UserAccessResponse {

    private Long id;
    private Long managerId;
    private String permissionName;
    private String roleName;
    private BigDecimal transactionLimit = new BigDecimal("0.00");
}
