package com.waya.wayaauthenticationservice.pojo.fraud;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@AllArgsConstructor
public class FraudEventRequestDTO {

    @NotBlank(message = "ruleName must be Passed")
    private String eventRuleName;

    private Long userId;
}
