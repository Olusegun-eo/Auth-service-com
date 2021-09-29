package com.waya.wayaauthenticationservice.pojo.fraud;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class FraudEventResponseDTO{

    private String eventId;
    private Long eventRuleId;
    private String eventRuleName;
    private Long userId;
    private String expireAt;
    private boolean isDeleted;
    private boolean isActive;
    private Integer noOfTimes;
    private Date createdAt;
    private Date updatedAt;
}