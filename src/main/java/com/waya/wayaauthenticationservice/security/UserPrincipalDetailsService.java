package com.waya.wayaauthenticationservice.security;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.waya.wayaauthenticationservice.entity.Users;
import com.waya.wayaauthenticationservice.exception.ErrorMessages;
import com.waya.wayaauthenticationservice.repository.UserRepository;

@Service
@Transactional
public class UserPrincipalDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

	public UserPrincipalDetailsService() {
		super();
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

		Users userEntity = this.userRepository.findByEmailOrPhoneNumber(username).orElseThrow(
				() -> new UsernameNotFoundException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage() + username));

		UserPrincipal user = new UserPrincipal(userEntity);
		return user;
	}

}
