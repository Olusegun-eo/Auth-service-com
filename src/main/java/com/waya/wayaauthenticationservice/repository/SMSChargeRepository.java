package com.waya.wayaauthenticationservice.repository;

import com.waya.wayaauthenticationservice.entity.SMSCharge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SMSChargeRepository extends JpaRepository<SMSCharge, UUID> {
    Optional<SMSCharge> findById(Long id);

    @Query("SELECT i FROM SMSAlertConfig i WHERE i.active = true")
    Optional<SMSCharge> findByActive();
}
