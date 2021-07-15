package com.waya.wayaauthenticationservice.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.waya.wayaauthenticationservice.entity.Role;

@Repository
public interface RolesRepository extends JpaRepository<Role, Long> {
	
	 Optional<Role> findByName(String name);
}
