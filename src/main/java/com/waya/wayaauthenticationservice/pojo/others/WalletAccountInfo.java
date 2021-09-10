package com.waya.wayaauthenticationservice.pojo.others;

import com.waya.wayaauthenticationservice.response.NewWalletResponse;

import java.util.Date;

public class WalletAccountInfo {
    public Date timeStamp;
    public boolean status;
    public String message;
    public NewWalletResponse data;

    public WalletAccountInfo(Date timeStamp, boolean status, String message, NewWalletResponse data) {
        super();
        this.timeStamp = timeStamp;
        this.status = status;
        this.message = message;
        this.data = data;
    }
}
