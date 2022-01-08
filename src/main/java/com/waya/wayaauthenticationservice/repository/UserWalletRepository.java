package com.waya.wayaauthenticationservice.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.waya.wayaauthenticationservice.entity.UserWallet;

@Repository
public interface UserWalletRepository extends JpaRepository<UserWallet, Long> {
	
	Optional<UserWallet> findByUserId(Long userId);

}
