package com.waya.wayaauthenticationservice.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.waya.wayaauthenticationservice.entity.CooperateUser;

@Repository
public interface CooperateUserRepository extends JpaRepository<CooperateUser, Long>{

	Optional<CooperateUser> findByUserId(Long userId);
}
