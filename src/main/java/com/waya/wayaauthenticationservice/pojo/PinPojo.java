package com.waya.wayaauthenticationservice.pojo;

import lombok.Getter;
import lombok.Setter;

import java.util.Optional;

@Getter
@Setter
public class PinPojo {
    private Long userId;
    private int pin;
    private Optional<String> email;
}
