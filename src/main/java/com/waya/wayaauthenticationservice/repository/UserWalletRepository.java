package com.waya.wayaauthenticationservice.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.waya.wayaauthenticationservice.entity.UserWallet;

@Repository
public interface UserWalletRepository extends JpaRepository<UserWallet, Long> {
	
	Optional<UserWallet> findByUserId(Long userId);
	
	@Query(value = "SELECT u FROM UserWallet u WHERE u.usertype = :value AND u.isDeleted = false ORDER BY id")
	Page<UserWallet> findByIndividualCorporate(@Param("value") String value, Pageable pageable);

}
