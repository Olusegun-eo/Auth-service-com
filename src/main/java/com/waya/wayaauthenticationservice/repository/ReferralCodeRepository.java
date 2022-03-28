package com.waya.wayaauthenticationservice.repository;

import com.waya.wayaauthenticationservice.entity.ReferralCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ReferralCodeRepository extends JpaRepository<ReferralCode, UUID> {

    Optional<ReferralCode> findByUserId(String userId);

    @Query("select u from ReferralCode u where u.userId =:userId")
    Optional<ReferralCode> getReferralCodeByUserId(@Param("userId") String userId);

    @Query("select u from ReferralCode u where TRIM(u.referralCode) = TRIM(:referralCode)")
    Optional<ReferralCode> getReferralCodeByCode(@Param("referralCode") String code);

    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM ReferralCode u " +
            "WHERE (UPPER(u.referralCode) = UPPER(:referralCode) OR u.userId = :userId)")
    boolean existsByUserIdOrRefCode(@Param("referralCode") String referralCode,
                                    @Param("userId") String userId);

}