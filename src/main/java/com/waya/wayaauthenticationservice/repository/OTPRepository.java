package com.waya.wayaauthenticationservice.repository;

import com.waya.wayaauthenticationservice.entity.OTPBase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.Optional;

@SuppressWarnings("ALL")
public interface OTPRepository extends JpaRepository<OTPBase, Long> {

    @Transactional
    @Modifying
    @Query(value = "update otpbase set expiry_date =:newExpiryDate, valid =:isValid where phone_number =:phoneNumber ", nativeQuery = true)
    void invalidatePreviousRecords(@Param("phoneNumber") String phoneNumber, @Param("newExpiryDate") LocalDateTime newExpiryDate, @Param("isValid") Boolean isValid);

    @Transactional
    @Modifying
    @Query(value = "update otpbase set expiry_date =:newExpiryDate, valid =:isValid where email =:email ", nativeQuery = true)
    void invalidatePreviousRecordsViaEmail(@Param("email") String email, @Param("newExpiryDate") LocalDateTime newExpiryDate, @Param("isValid") Boolean isValid);

    @Query(value = "select * from otpbase where email =:email and code= :otp", nativeQuery = true)
    Optional<OTPBase> getOtpDetailsViaEmail(String email, Integer otp);

    @Query(value = "select * from otpbase where phone_number =:phoneNumber and code= :otp", nativeQuery = true)
    Optional<OTPBase> getOtpDetailsViaPhoneNumber(String phoneNumber, Integer otp);

    @Transactional
    @Modifying
    @Query(value = "update otpbase set expiry_date =:expiryDate, valid =:isValid where phone_number=:phoneNumber and id = :id", nativeQuery = true)
    void updateToken(@Param("phoneNumber") String phoneNumber, @Param("id") Long id, @Param("expiryDate") LocalDateTime expiryDate,
                     @Param("isValid") Boolean isValid);

    @Transactional
    @Modifying
    @Query(value = "update otpbase set expiry_date =:expiryDate, valid =:isValid where email=:email and id = :id", nativeQuery = true)
    void updateTokenForEmail(@Param("email") String email, @Param("id") Long id, @Param("expiryDate") LocalDateTime expiryDate,
                             @Param("isValid") Boolean isValid);


}