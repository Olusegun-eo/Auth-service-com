package com.waya.wayaauthenticationservice.pojo.others;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import java.util.UUID;

@Getter
@Setter
public class OtherDetailsRequest {
    @NotBlank(message = "please enter your organisation name")
    private String organisationName;

    @NotBlank(message = "please enter your organisation type")
    private String organisationType;

    @NotBlank(message = "please enter your business type")
    private String businessType;

    @NotBlank(message = "please enter the other details id")
    @JsonIgnore
    private UUID otherDetailsId;
}
