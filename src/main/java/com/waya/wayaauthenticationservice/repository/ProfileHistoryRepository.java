package com.waya.wayaauthenticationservice.repository;

import com.waya.wayaauthenticationservice.entity.ProfileHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@SuppressWarnings("all")
@Repository
public interface ProfileHistoryRepository extends JpaRepository<ProfileHistory, Long> {

}

