package com.waya.wayaauthenticationservice.repository;

import com.waya.wayaauthenticationservice.entity.Roles;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RolesRepository extends JpaRepository<Roles, Long> {
    public Roles findByName(String name);
}
