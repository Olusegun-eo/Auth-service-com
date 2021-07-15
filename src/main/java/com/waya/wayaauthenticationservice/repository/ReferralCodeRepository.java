package com.waya.wayaauthenticationservice.repository;

import com.waya.wayaauthenticationservice.entity.ReferralCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface ReferralCodeRepository extends JpaRepository<ReferralCode, UUID> {

    Optional<ReferralCode> findByUserId(String userId);

    @Query(value = "select * from referral_code where user_id =:userId", nativeQuery = true)
    ReferralCode getReferralCodeByUserId(String userId);
}