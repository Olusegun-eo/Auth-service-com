package com.waya.wayaauthenticationservice.repository;

import com.waya.wayaauthenticationservice.entity.OtherDetails;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OtherDetailsRepository extends JpaRepository<OtherDetails, UUID> {
}
