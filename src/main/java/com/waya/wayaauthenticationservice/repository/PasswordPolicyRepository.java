package com.waya.wayaauthenticationservice.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.waya.wayaauthenticationservice.entity.PasswordPolicy;
import com.waya.wayaauthenticationservice.entity.Users;

@Repository
public interface PasswordPolicyRepository extends JpaRepository<PasswordPolicy, Long> {
	
	Optional<PasswordPolicy> findByUser(Users user);
	
	@Query("SELECT u FROM PasswordPolicy u WHERE u.user = (:user)" + " AND u.del_flg = false")
	Optional<PasswordPolicy> findByUserActive(Users user);

}
