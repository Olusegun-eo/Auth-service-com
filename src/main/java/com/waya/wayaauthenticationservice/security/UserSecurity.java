package com.waya.wayaauthenticationservice.security;

import java.util.Collection;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.waya.wayaauthenticationservice.entity.Roles;
import com.waya.wayaauthenticationservice.entity.Users;
import com.waya.wayaauthenticationservice.exception.ErrorMessages;
import com.waya.wayaauthenticationservice.exception.UserServiceException;
import com.waya.wayaauthenticationservice.repository.UserRepository;

import lombok.extern.slf4j.Slf4j;

@Component("userSecurity")
@Slf4j
public class UserSecurity {

	private UserRepository userRepo;

	public UserSecurity(UserRepository userRepo) {
		this.userRepo = userRepo;
	}

	public boolean useHierarchy(Long id, Authentication authentication) {
		Users user = ((UserPrincipal) authentication.getPrincipal()).getUser()
				.orElseThrow(() -> new UserServiceException(ErrorMessages.AUTHENTICATION_FAILED.getErrorMessage()));

		if (user.getId() == id)
			return true;

		Users returnObj = this.userRepo.findById(id)
				.orElseThrow(() -> new UserServiceException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage() + id));

		boolean isOga = compareRoles(returnObj, user) > 0;

		log.info("isOga returned {}", isOga);
		return isOga;
	}

	public boolean useHierarchy(String emailOrPhoneNumber, Authentication authentication) {

		Users user = ((UserPrincipal) authentication.getPrincipal()).getUser()
				.orElseThrow(() -> new UserServiceException(ErrorMessages.AUTHENTICATION_FAILED.getErrorMessage()));

		if (user.getEmail().equals(emailOrPhoneNumber))
			return true;

		if (user.getPhoneNumber().equals(emailOrPhoneNumber))
			return true;

		Users returnObj = this.userRepo.findByEmailOrPhoneNumber(emailOrPhoneNumber).orElseThrow(
				() -> new UserServiceException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage() + emailOrPhoneNumber));

		boolean isOga = compareRoles(returnObj, user) > 0;

		log.info("isOga returned {}", isOga);
		return isOga;
	}

	private int compareRoles(Users targetEmp, Users authEmp) {

		Integer authEmpLevel = 0;
		Integer returnEmp = 0;

		// TODO: To Compare both User Roles and See who is greater
		boolean role = roleCheck(authEmp.getRolesList(), "ROLE_ADMIN");
		if (role)
			authEmpLevel = 1;

		log.info("Authenticating User level is {}, Target User level is {}", authEmpLevel, returnEmp);
		return authEmpLevel.compareTo(returnEmp);
	}

	public boolean roleCheck(Collection<Roles> rolesList, String role) {
		return rolesList.stream().anyMatch(e -> e.getName().equals(role));
	}

}
