package com.waya.wayaauthenticationservice.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.waya.wayaauthenticationservice.entity.CoporateUser;

@Repository
public interface CooperateUserRepository extends JpaRepository<CoporateUser, Long>{

	Optional<CoporateUser> findByUserId(Long userId);
}
