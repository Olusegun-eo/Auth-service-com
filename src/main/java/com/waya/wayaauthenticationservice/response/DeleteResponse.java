package com.waya.wayaauthenticationservice.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeleteResponse {
    private String code;
    private String message;
    private String error;
}