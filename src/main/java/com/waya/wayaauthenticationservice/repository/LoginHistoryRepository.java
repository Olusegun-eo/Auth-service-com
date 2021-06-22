package com.waya.wayaauthenticationservice.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.waya.wayaauthenticationservice.entity.LoginHistory;
import com.waya.wayaauthenticationservice.entity.Users;

@Repository
public interface LoginHistoryRepository extends JpaRepository<LoginHistory, Long> {
    List<LoginHistory> findByUser(Users user);

    LoginHistory findTop1ByUserOrderByLoginDateDesc(Users user);
}
