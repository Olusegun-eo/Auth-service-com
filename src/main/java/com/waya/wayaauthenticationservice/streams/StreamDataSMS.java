package com.waya.wayaauthenticationservice.streams;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class StreamDataSMS {

    private List<RecipientsSMS> recipients;

    private String message;
}
