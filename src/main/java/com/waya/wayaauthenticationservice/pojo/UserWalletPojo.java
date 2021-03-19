package com.waya.wayaauthenticationservice.pojo;

import com.waya.wayaauthenticationservice.entity.Users;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserWalletPojo {

    Users user;
    String accountNo;

}
