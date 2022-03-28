package com.waya.wayaauthenticationservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.waya.wayaauthenticationservice.entity.UserSetup;

@Repository
public interface UserSetupRepository extends JpaRepository<UserSetup, Long> {
   
	 @Query("select w from UserSetup w join fetch w.user u where u.id = :id" +
	            " and u.isDeleted = false")
    UserSetup findByUserId(@Param("id") long id);
	 
	
	 @Query("select w from UserSetup w join fetch w.user u where u.id = :id" +
	            " and u.isDeleted = false and w.isUpdated = false")
 UserSetup GetByUserId(@Param("id") long id);

}
