package com.waya.wayaauthenticationservice.pojo.others;


import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class LoginHistoryPojo {

    private long id;
    private String ip;
    private String device;
    private String city;
    private String province;
    private String country;

    @NotNull
    private long userId;
}