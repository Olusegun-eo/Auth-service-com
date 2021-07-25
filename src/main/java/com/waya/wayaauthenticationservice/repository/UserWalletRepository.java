package com.waya.wayaauthenticationservice.repository;

import com.waya.wayaauthenticationservice.entity.UserWallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserWalletRepository extends JpaRepository<UserWallet, Long> {

    @Query("select w from UserWallet w join fetch w.user where w.id = :id" +
            " and w.isDeleted = false and w.accountType = :accountType")
    UserWallet findUserWalletsById(@Param("id") long id, @Param("accountType") String accountType);

}