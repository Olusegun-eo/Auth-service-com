package com.waya.wayaauthenticationservice.response;

import com.waya.wayaauthenticationservice.pojo.others.TransactionResponsePojo;

import java.util.Date;

public class ResponseObj {
    public Date timeStamp;
    public boolean status;
    public String message;
    public TransactionResponsePojo data;

    public ResponseObj() {
    }

    public ResponseObj(Date timeStamp, boolean status, String message, TransactionResponsePojo data) {
        super();
        this.timeStamp = timeStamp;
        this.status = status;
        this.message = message;
        this.data = data;
    }

}
