package com.waya.wayaauthenticationservice.pojo.others;


import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.Date;

@Data
public class LoginHistoryPojo {

    private long id;
    private String ip;
    private String device;
    private String city;
    private String province;
    private String country;
    private Date loginDate;

    @NotNull
    private long userId;
}