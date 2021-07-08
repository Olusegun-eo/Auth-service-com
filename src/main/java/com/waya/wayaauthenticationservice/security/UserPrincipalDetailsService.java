package com.waya.wayaauthenticationservice.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.waya.wayaauthenticationservice.entity.Users;
import com.waya.wayaauthenticationservice.exception.ErrorMessages;
import com.waya.wayaauthenticationservice.repository.UserRepository;

@Service
public class UserPrincipalDetailsService implements UserDetailsService {

	private UserRepository userRepo;

	public UserPrincipalDetailsService(UserRepository userRepo) {
		this.userRepo = userRepo;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		Users userEntity = this.userRepo.findByEmailOrPhoneNumber(username)
					.orElseThrow(() -> new UsernameNotFoundException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage() + username));

		UserPrincipal user = new UserPrincipal(userEntity);
		return user;
	}

}
