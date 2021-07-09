package com.waya.wayaauthenticationservice.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.waya.wayaauthenticationservice.entity.CorporateUser;

@Repository
public interface CorporateUserRepository extends JpaRepository<CorporateUser, Long>{

	Optional<CorporateUser> findByUserId(Long userId);
}
