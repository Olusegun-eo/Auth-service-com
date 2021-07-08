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
public class GeneralResponse {
    private String timestamp;
    private String message;
    private Optional<Object>  data;
    private boolean status;
}
