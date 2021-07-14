package com.waya.wayaauthenticationservice.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.waya.wayaauthenticationservice.entity.SMSAlertConfig;

@Repository
public interface SMSAlertConfigRepository extends JpaRepository<SMSAlertConfig, UUID> {

    @Query("SELECT i FROM SMSAlertConfig i WHERE i.phoneNumber = :phoneNumber")
    Optional<SMSAlertConfig> findByPhoneNumber(String phoneNumber);
}
