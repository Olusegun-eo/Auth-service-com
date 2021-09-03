package com.waya.wayaauthenticationservice.repository;

import com.waya.wayaauthenticationservice.entity.UserHistory;
import com.waya.wayaauthenticationservice.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface UserHistoryRepository extends JpaRepository<UserHistory, Long>, JpaSpecificationExecutor<Users> {

}
