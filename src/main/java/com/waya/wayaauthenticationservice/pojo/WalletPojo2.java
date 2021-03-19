package com.waya.wayaauthenticationservice.pojo;


import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Setter
@Getter
public class WalletPojo2 {

    private Long id;
    private String accountNo;
    private String accountName;
    private double balance = 0.00;
    private boolean isDefault = false;

}