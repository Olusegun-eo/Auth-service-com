package com.waya.wayaauthenticationservice.repository;

import com.waya.wayaauthenticationservice.entity.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@SuppressWarnings("all")
@Repository
public interface ProfileRepository extends JpaRepository<Profile, UUID> {

    @Query(value = "update m_user_profile set deleted =:deleted where user_id=:userId", nativeQuery = true)
    Optional<Profile> deleteProfileByUserId(boolean deleted, String userId);

    @Query(value = "select * from m_user_profile where email =:email and deleted=:deleted", nativeQuery = true)
    Optional<Profile> findByEmail(boolean deleted, String email);

    @Query(value = "select * from m_user_profile where user_id =:userId and deleted =:deleted", nativeQuery = true)
    Optional<Profile> findByUserId(boolean deleted, String userId);

    @Async
    @Query(value = "select * from m_user_profile where user_id =:userId and deleted =:deleted", nativeQuery = true)
    CompletableFuture<Profile> findByUserIdAsync(boolean deleted, String userId);

    @Query(value = "select * from m_user_profile where id =:profileId and deleted =:deleted", nativeQuery = true)
    Optional<Profile> findByProfileId(boolean deleted, @Param("profileId") UUID profileId);

    @Query(value = "select * from m_user_profile where id =:profileId and deleted =:deleted and user_id=:userId", nativeQuery = true)
    Optional<Profile> findByProfileIdAndUserId(boolean deleted, UUID profileId, String userId);

    @Query(value = "select * from m_user_profile pm where lower(CONCAT(pm.first_name, ' ', pm.surname)) like :userName and deleted =:deleted", nativeQuery = true)
    List<Profile> searchByName(String userName, boolean deleted);

    @Query(value = "select * from m_user_profile where phone_number LIKE :phoneNumber and deleted =:deleted", nativeQuery = true)
    List<Profile> searchByPhoneNumber(String phoneNumber, boolean deleted);

    @Query(value = "select * from m_user_profile where lower(email) LIKE :email and deleted =:deleted", nativeQuery = true)
    List<Profile> searchByEmail(String email, boolean deleted);

    @Query(value = "select * from m_user_profile where lower(organisation_name) LIKE :organizationName and deleted =:deleted", nativeQuery = true)
    List<Profile> searchByCompanyName(String organizationName, boolean deleted);

    @Query(value = "select * from m_user_profile where referral =:referralCode and deleted =:deleted LIMIT :limit OFFSET :offset", nativeQuery = true)
    List<Profile> findAllByReferralCode(String referralCode, int limit, int offset, boolean deleted);

    @Query(value = "select * from m_user_profile where referral =:referral and deleted =:deleted", nativeQuery = true)
    Optional<Profile> findByReferral(boolean deleted, String referral);

    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM Profile u " +
            "WHERE UPPER(u.email) = UPPER(:email) AND u.deleted = false")
    boolean existsByEmail(@Param("email") String email);
}

