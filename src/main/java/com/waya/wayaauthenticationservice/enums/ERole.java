package com.waya.wayaauthenticationservice.enums;

import java.util.Arrays;
import java.util.stream.Collectors;

public enum ERole {

    ROLE_OWNER_ADMIN, ROLE_SUPER_ADMIN,
    ROLE_APP_ADMIN, ROLE_CORP_ADMIN,
    ROLE_CORP, ROLE_USER,ROLE_MERCHANT,ROLE_AGENT,ROLE_AGGREGATOR;

    public static String getRoleHierarchy() {
        return Arrays.stream(ERole.values())
                .map(ERole::getRole)
                .collect(Collectors.joining(" > "));
    }

    public String getRole() {
        return name();
    }
}
