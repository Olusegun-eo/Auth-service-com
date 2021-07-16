package com.waya.wayaauthenticationservice.enums;

import lombok.Getter;

@Getter
public enum  DeleteType {
    DELETE("DELETE"),
    RESTORE("RESTORE"),
    NONE("NONE");


    private String key;

    DeleteType(String key) {
        this.key = key;
    }
}
