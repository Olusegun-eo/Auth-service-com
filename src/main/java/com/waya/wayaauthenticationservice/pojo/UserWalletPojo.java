package com.waya.wayaauthenticationservice.pojo;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserWalletPojo {

    UserProfileResponsePojo user;
    String accountNo;
    Long walletId;

}
