package com.waya.wayaauthenticationservice.pojo.others;

import lombok.Data;

@Data
public class DeviceTokenRequest {
    private String token;
    private Long userId;
}
