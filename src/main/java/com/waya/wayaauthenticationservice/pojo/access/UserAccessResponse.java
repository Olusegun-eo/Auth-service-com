package com.waya.wayaauthenticationservice.pojo.access;

import lombok.Data;
import lombok.ToString;

import java.math.BigDecimal;

@Data
@ToString
public class UserAccessResponse {

    private Long id;
    private Long managerId;
    private String permissionName;
    private String roleName;
    private BigDecimal transactionLimit = new BigDecimal("0.00");

}
