package com.waya.wayaauthenticationservice.dao;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.waya.wayaauthenticationservice.pojo.userDTO.UserProfilePojo;

public interface ProfileServiceDAO {
	
	public Integer getProfileCount(String user_id, String phone);
	
	public List<UserProfilePojo> GetAllUserProfile();
	
	public UserProfilePojo GetUserProfile(Long user_id);
	
	public UserProfilePojo GetSimulatedUserProfile(Long user_id);
	
	public List<UserProfilePojo> GetAllSimulatedUserProfile();
	
	public List<UserProfilePojo> GetAllUserProfile(Sort sort);
	
	public Page<UserProfilePojo> GetAllUserProfile(Pageable page);
	
	public List<UserProfilePojo> GetAllSimulatedUserProfile(Sort sort);
	
	public Page<UserProfilePojo> GetAllSimulatedUserProfile(Pageable page);

}
