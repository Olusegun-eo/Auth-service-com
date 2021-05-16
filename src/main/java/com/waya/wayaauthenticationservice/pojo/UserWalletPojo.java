package com.waya.wayaauthenticationservice.pojo;

import com.waya.wayaauthenticationservice.entity.Users;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserWalletPojo {

    Users user;
    String accountNo;
    Long walletId;

}
