package com.waya.wayaauthenticationservice.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.waya.wayaauthenticationservice.entity.Roles;

@Repository
public interface RolesRepository extends JpaRepository<Roles, Long> {
	
	 Optional<Roles> findByName(String name);
}
