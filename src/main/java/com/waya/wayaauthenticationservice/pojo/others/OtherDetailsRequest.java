package com.waya.wayaauthenticationservice.pojo.others;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import java.util.UUID;

@Getter
@Setter
public class OtherDetailsRequest {
    @NotBlank(message = "please enter your organisation name")
    private String organisationName;
    private String organisationEmail;
    private String organisationPhone;
    private String organizationCity;
    private String organizationAddress;
    private String organizationState;


    private String organisationType;

    @NotBlank(message = "please enter your business type")
    private String businessType;

    @NotBlank(message = "please enter the other details id")
    private UUID otherDetailsId;
}
