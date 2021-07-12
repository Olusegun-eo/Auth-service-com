package com.waya.wayaauthenticationservice.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.waya.wayaauthenticationservice.entity.Roles;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.waya.wayaauthenticationservice.entity.Users;

@Repository
public interface UserRepository extends JpaRepository<Users, Long> {

    @Query("SELECT u FROM Users u " +
            "WHERE UPPER(u.email) = UPPER(:value) " +
            " AND u.isDeleted = false")
    Optional<Users> findByEmailIgnoreCase(@Param("value") String value);

    @Query("SELECT u FROM Users u WHERE u.phoneNumber LIKE CONCAT('%', ?1)" +
            " AND u.isDeleted = false")
    Optional<Users> findByPhoneNumber(String phoneNumber);

    @Query(value =
            "SELECT _user FROM Users _user " +
            "WHERE UPPER(_user.email) = UPPER(:value) OR " +
            "_user.phoneNumber LIKE CONCAT('%', :value) " +
            "AND _user.isDeleted = false"
    )
    Optional<Users> findByEmailOrPhoneNumber(@Param("value") String value);

    Page<Users> findByRolesListIn(Collection<Roles> roles, Pageable pageable);

    Page<Users> findUserByIsCorporate(boolean value, Pageable pageable);

    boolean existsByUserId(String userId);

    @Query("SELECT u FROM Users u WHERE UPPER(u.userId) = UPPER(:userId)" +
            " AND u.isDeleted = false")
    Optional<Users> findByUserId(@Param("userId") String userId);
}
