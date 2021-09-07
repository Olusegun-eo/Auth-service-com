package com.waya.wayaauthenticationservice.service;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;

import com.waya.wayaauthenticationservice.entity.Users;

public interface SimulatedService {
	
	ResponseEntity<?> getUserByEmail(String email);
	
	Page<Users> getAllUsers(int page, int size, String searchString);

}
