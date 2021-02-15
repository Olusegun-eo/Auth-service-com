package com.waya.wayaauthenticationservice.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WalletPojo {

    private String email;
    private String firstName;
    private String lastName;
    private String mobileNumber;

}