package com.waya.wayaauthenticationservice.pojo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ProfilePojo2 extends ProfilePojo {
    private String businessType;
    private String organisationEmail;
    private String organisationName;
    private String organisationType;
    private String referralCode;
}