package com.waya.wayaauthenticationservice.repository;

import com.waya.wayaauthenticationservice.entity.ReferralCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ReferralCodeRepository extends JpaRepository<ReferralCode, UUID> {

    Optional<ReferralCode> findByUserId(String userId);

    @Query(value = "select * from m_referral_tbl where user_id =:userId", nativeQuery = true)
    ReferralCode getReferralCodeByUserId(String userId);

    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM ReferralCode u " +
            "WHERE UPPER(u.referralCode) = UPPER(:referralCode) OR u.userId = :userId")
    boolean existsByUserIdOrRefCode(@Param("referralCode") String referralCode, @Param("userId") String userId);

}