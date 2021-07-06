package com.waya.wayaauthenticationservice.streams;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class StreamPayload<T> {
    private T data;

    private String initiator;

    private String eventType;

    private String token;

    @JsonIgnore
    private String key;
}
