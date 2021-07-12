package com.waya.wayaauthenticationservice.response.notification;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class DataPojo {
    private String message;
    private List<NamesPojo> names;
}
