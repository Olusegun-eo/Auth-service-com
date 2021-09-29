package com.waya.wayaauthenticationservice.util;

import java.util.Optional;

public enum ReferralBonusStatus {
    PENDING,
    PAID;

    public static Optional<ReferralBonusStatus> find(String value){
        if (CommonUtils.isNonEmpty(value)){
            try {
                return Optional.of(ReferralBonusStatus.valueOf(value.toUpperCase()));
            } catch (IllegalArgumentException e) {
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

}
