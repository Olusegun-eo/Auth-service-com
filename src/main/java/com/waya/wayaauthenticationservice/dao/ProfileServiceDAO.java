package com.waya.wayaauthenticationservice.dao;

import java.util.List;

import com.waya.wayaauthenticationservice.pojo.userDTO.UserProfilePojo;

public interface ProfileServiceDAO {
	
	public Integer getProfileCount(String user_id, String phone);
	
	public List<UserProfilePojo> GetAllUserProfile();
	
	public UserProfilePojo GetUserProfile(Long user_id);
	
	public UserProfilePojo GetSimulatedUserProfile(Long user_id);
	
	public List<UserProfilePojo> GetAllSimulatedUserProfile();

}
