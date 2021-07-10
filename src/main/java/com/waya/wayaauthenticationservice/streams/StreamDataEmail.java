package com.waya.wayaauthenticationservice.streams;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class StreamDataEmail {
    private List<RecipientsEmail> names;

    private String message;
}
