package com.waya.wayaauthenticationservice.pojo;


import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Setter
@Getter
public class LoginHistoryPojo {
    private long id;
    private String ip;
    private String device;
    private String city;
    private String province;
    private String country;
    private long userId;
}