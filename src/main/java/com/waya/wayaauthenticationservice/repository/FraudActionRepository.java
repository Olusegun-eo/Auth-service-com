package com.waya.wayaauthenticationservice.repository;

import com.waya.wayaauthenticationservice.entity.FraudAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface FraudActionRepository extends JpaRepository<FraudAction, UUID> {

    @Query(value = "select * from m_auth_fraud_base where is_deleted = false " +
            "and fraud_type = ?1 and user_id = ?2", nativeQuery = true)
    Optional<FraudAction> findActionByUserId(String fraudType, Long userId);

}