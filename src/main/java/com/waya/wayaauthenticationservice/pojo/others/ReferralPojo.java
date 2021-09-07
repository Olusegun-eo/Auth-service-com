package com.waya.wayaauthenticationservice.pojo.others;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.waya.wayaauthenticationservice.entity.Profile;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
public class ReferralPojo {
    private String referralUser;
    private String referredBy;
    private String referralPhone;
    private String referralEmail;
    private String referralLocation;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+1")
    private Date dateJoined;
    private List<Profile> usersReferred;
    private String referralCode;
    private BigDecimal earnings;

}
