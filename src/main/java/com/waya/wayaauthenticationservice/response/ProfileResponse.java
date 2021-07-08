package com.waya.wayaauthenticationservice.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@JsonInclude
public class ProfileResponse {

    private String timeStamp;
    private String message;
//    private Optional<OTPResponse> data;
    private boolean status;
}