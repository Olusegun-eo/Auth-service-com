package com.waya.wayaauthenticationservice.repository;

import com.waya.wayaauthenticationservice.entity.ReferralBonusEarning;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReferralBonusEarningRepository extends JpaRepository<ReferralBonusEarning, Long> {

    @Query("select c from ReferralBonusEarning c where c.userId = :userId")
    List<ReferralBonusEarning> findAllByUserId(@Param("userId") String userId);
}
