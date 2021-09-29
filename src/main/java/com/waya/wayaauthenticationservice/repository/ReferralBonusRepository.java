package com.waya.wayaauthenticationservice.repository;

import com.waya.wayaauthenticationservice.entity.ReferralBonus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface ReferralBonusRepository extends JpaRepository<ReferralBonus, Long> {

    @Query("select r from ReferralBonus r where r.active =:active")
    ReferralBonus findByActive(Boolean active);

    Optional<ReferralBonus> findByAmount(BigDecimal amount);

}
