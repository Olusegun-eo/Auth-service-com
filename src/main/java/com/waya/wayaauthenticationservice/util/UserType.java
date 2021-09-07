package com.waya.wayaauthenticationservice.util;

import java.util.Optional;

public enum UserType {

    PERSONAL_USER,
    CORPORATE_USER,
    CORPORATE_USER_AGENT,
    CORPORATE_USER_AGGREGATOR;

    public static Optional<UserType> find(String value){
        if (CommonUtils.isNonEmpty(value)){
            try {
                return Optional.of(UserType.valueOf(value.toUpperCase()));
            } catch (IllegalArgumentException e) {
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

}