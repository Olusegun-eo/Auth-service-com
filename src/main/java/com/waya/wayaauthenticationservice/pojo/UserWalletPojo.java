package com.waya.wayaauthenticationservice.pojo;


import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserWalletPojo {

    private UserProfileResponsePojo user;
    private List<WalletAccount> walletAccounts = new ArrayList<>();
    private String message;
}
