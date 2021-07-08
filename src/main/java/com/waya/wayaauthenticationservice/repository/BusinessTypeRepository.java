package com.waya.wayaauthenticationservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.waya.wayaauthenticationservice.entity.BusinessType;

@Repository
public interface BusinessTypeRepository extends JpaRepository<BusinessType, Long> {

}
