package com.waya.wayaauthenticationservice.service;

import org.springframework.http.ResponseEntity;

public interface SimulatedService {
	
	ResponseEntity<?> getUserByEmail(String email);

}
