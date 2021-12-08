package com.waya.wayaauthenticationservice.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.waya.wayaauthenticationservice.entity.CorporateUser;

public interface CorporateUserRepository extends JpaRepository<CorporateUser, Long> {
	
	@Query(value = "SELECT * FROM m_corporate u WHERE UPPER(u.email) = UPPER(:value) AND u.is_deleted = false", nativeQuery = true)
	Optional<CorporateUser> findByEmailIgnoreCase(@Param("value") String value);

	@Query("SELECT u FROM CorporateUser u WHERE u.phoneNumber LIKE CONCAT('%', ?1) AND u.isDeleted = false")
	Optional<CorporateUser> findByPhoneNumber(String phoneNumber);

	@Query(value = "SELECT u FROM CorporateUser u WHERE (UPPER(u.email) = UPPER(:value) OR "
			+ "u.phoneNumber LIKE CONCAT('%', :value)) AND u.isDeleted = false")
	Optional<CorporateUser> findByEmailOrPhoneNumber(@Param("value") String value);

}
