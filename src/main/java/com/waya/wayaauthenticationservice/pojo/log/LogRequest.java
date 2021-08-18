package com.waya.wayaauthenticationservice.pojo.log;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LogRequest {

    private Long id;
    private String action;
    private String jsonRequest;
    private String jsonResponse;
    private String message;
    private String module;
    private LocalDateTime requestDate;
    private LocalDateTime responseDate;
    private Long userId;

    public LogRequest() {
        this.requestDate = LocalDateTime.now();
        this.responseDate = LocalDateTime.now();
    }
}
