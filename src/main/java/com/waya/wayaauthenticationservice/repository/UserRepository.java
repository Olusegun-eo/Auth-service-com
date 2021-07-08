package com.waya.wayaauthenticationservice.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.waya.wayaauthenticationservice.entity.Users;

@Repository
public interface UserRepository extends JpaRepository<Users, Long> {

    Optional<Users> findByEmailOrPhoneNumber(String email, String phone);

    Optional<Users> findByEmailIgnoreCase(String email);

    @Query("SELECT u FROM Users u WHERE u.phoneNumber LIKE CONCAT('%', ?1)")
    Optional<Users> findByPhoneNumber(String phoneNumber);

    @Query(value =
            "SELECT _user FROM Users _user " +
            "WHERE " +
            "UPPER(_user.email) = UPPER(:value) OR " +
            "_user.phoneNumber LIKE CONCAT('%', :value)"
    )
    Optional<Users> findByEmailOrPhoneNumber(@Param("value") String value);
}
