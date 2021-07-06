package com.waya.wayaauthenticationservice.streams;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class StreamDataSMS {
    private List<RecipientsSMS> recipients;

    private String message;
}
