package com.waya.wayaauthenticationservice.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Optional;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class OTPResponse {

    private Optional<String> timestamp;
    private Optional<String> message;
    private Optional<Object>  data;
    private Optional<Integer> status;
    private Optional<String> error;
}
