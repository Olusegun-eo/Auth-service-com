package com.waya.wayaauthenticationservice.response;

import com.waya.wayaauthenticationservice.pojo.WalletPojo2;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Optional;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class WalletResponse {
    private Long timestamp;
    private boolean status;
    private String message;
    private WalletPojo2 data;

}