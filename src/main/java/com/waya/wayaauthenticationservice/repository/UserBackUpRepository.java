package com.waya.wayaauthenticationservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.waya.wayaauthenticationservice.entity.UsersBackUp;

public interface UserBackUpRepository  extends JpaRepository<UsersBackUp, Long> {
}
