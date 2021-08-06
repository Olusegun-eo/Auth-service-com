package com.waya.wayaauthenticationservice.repository;

import com.waya.wayaauthenticationservice.entity.OTPBase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface OTPRepository extends JpaRepository<OTPBase, Long> {
	
	@Query(value = "select * from m_otp_base t inner join (" +
        "select email, max(expiry_date) as maxDate" +
        " from m_otp_base group by email) tm on " +
        "t.email = tm.email and t.expiry_date = " +
        "tm.maxDate and t.email = :email " +
        " and t.request_type = :requestType ", nativeQuery = true)
	Optional<OTPBase> findLastOTPByEmail(@Param("email") String email,  @Param("requestType") String requestType);

    @Query(value = "select * from m_otp_base t inner join (" +
            "select phone_number, max(expiry_date) as maxDate" +
            " from m_otp_base group by phone_number) tm on " +
            "t.phone_number = tm.phone_number and t.expiry_date = " +
            "tm.maxDate and t.phone_number = :phoneNumber " +
            " and t.request_type = :requestType ", nativeQuery = true)
    Optional<OTPBase> findLastOTPByPhoneNumber(@Param("phoneNumber") String email,  @Param("requestType") String requestType);

    @Transactional
    @Modifying
    @Query(value = "update m_otp_base set expiry_date =:newExpiryDate, valid =:isValid where " +
            "phone_number =:phoneNumber and email =:email and request_type =:requestType", nativeQuery = true)
    void invalidatePreviousRecords(@Param("phoneNumber") String phoneNumber, @Param("email") String email,
                                   @Param("newExpiryDate") LocalDateTime newExpiryDate,
                                   @Param("isValid") Boolean isValid, @Param("requestType") String requestType);

    @Transactional
    @Modifying
    @Query(value = "update m_otp_base set expiry_date =:newExpiryDate, valid =:isValid where " +
            "phone_number =:phoneNumber and request_type =:requestType", nativeQuery = true)
    void invalidatePreviousRecords(@Param("phoneNumber") String phoneNumber, @Param("newExpiryDate") LocalDateTime newExpiryDate,
                                   @Param("isValid") Boolean isValid, @Param("requestType") String requestType);

    @Transactional
    @Modifying
    @Query(value = "update m_otp_base set expiry_date =:newExpiryDate, valid =:isValid where " +
            "email =:email and request_type =:requestType", nativeQuery = true)
    void invalidatePreviousRecordsViaEmail(@Param("email") String email, @Param("newExpiryDate") LocalDateTime newExpiryDate,
                                           @Param("isValid") Boolean isValid, @Param("requestType") String requestType);

    @Query(value = "select * from m_otp_base where email =:email and code= :otp and request_type =:requestType", nativeQuery = true)
    Optional<OTPBase> getOtpDetailsViaEmail(String email, Integer otp, @Param("requestType") String requestType);

    @Query(value = "select * from m_otp_base where phone_number =:phoneNumber and code= :otp and request_type =:requestType", nativeQuery = true)
    Optional<OTPBase> getOtpDetailsViaPhoneNumber(String phoneNumber, Integer otp, @Param("requestType") String requestType);

    @Transactional
    @Modifying
    @Query(value = "update m_otp_base set expiry_date =:expiryDate, valid =:isValid where phone_number=:phoneNumber " +
            "and id = :id and request_type =:requestType", nativeQuery = true)
    void updateToken(@Param("phoneNumber") String phoneNumber, @Param("id") Long id, @Param("expiryDate") LocalDateTime expiryDate,
                     @Param("isValid") Boolean isValid, @Param("requestType") String requestType);

    @Transactional
    @Modifying
    @Query(value = "update m_otp_base set expiry_date =:expiryDate, valid =:isValid where phone_number=:phoneNumber " +
            "and id = :id and email =:email and request_type =:requestType", nativeQuery = true)
    void updateTokenForJoint(@Param("phoneNumber") String phoneNumber, @Param("id") Long id,
                             @Param("email") String email, @Param("expiryDate") LocalDateTime expiryDate,
                             @Param("isValid") Boolean isValid, @Param("requestType") String requestType);

    @Transactional
    @Modifying
    @Query(value = "update m_otp_base set expiry_date =:expiryDate, valid =:isValid where email=:email " +
            "and id = :id and request_type =:requestType", nativeQuery = true)
    void updateTokenForEmail(@Param("email") String email, @Param("id") Long id, @Param("expiryDate") LocalDateTime expiryDate,
                             @Param("isValid") Boolean isValid, @Param("requestType") String requestType);

    @Modifying
    @Query("delete from OTPBase t where t.expiryDate < ?1")
    void deleteAllExpiredOTPs(LocalDateTime time);

    Integer deleteByExpiryDateLessThan(LocalDateTime time);

    @Query(value = "select * from m_otp_base where (phone_number =:value or email =:value)" +
            " and code =:otp and request_type =:requestType", nativeQuery = true)
    Optional<OTPBase> getOtpDetailsViaEmailOrPhoneNumber(String value, Integer otp, @Param("requestType") String requestType);
}