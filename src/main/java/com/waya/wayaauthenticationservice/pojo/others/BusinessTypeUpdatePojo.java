package com.waya.wayaauthenticationservice.pojo.others;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class BusinessTypeUpdatePojo {
    private Long id;
    @NotNull(message = "Business Type cannot be Null")
    private String businessType;
}
