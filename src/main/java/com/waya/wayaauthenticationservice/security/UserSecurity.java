package com.waya.wayaauthenticationservice.security;

import com.waya.wayaauthenticationservice.entity.Role;
import com.waya.wayaauthenticationservice.entity.Users;
import com.waya.wayaauthenticationservice.enums.ERole;
import com.waya.wayaauthenticationservice.exception.ErrorMessages;
import com.waya.wayaauthenticationservice.exception.UserServiceException;
import com.waya.wayaauthenticationservice.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Collection;

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

		if (user.getId().equals(id))
			return true;

		Users returnObj = this.userRepo.findById(false, id)
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
		String[] roles = ERole.getRoleHierarchy().split(">");

		Integer authEmpLevel = roles.length;
		Integer returnEmp = roles.length;

		for (int i = roles.length - 1; i >= 0; i--) {
			boolean authCheck = roleCheck(authEmp.getRoleList(), roles[i]);
			if (authCheck) authEmpLevel++;

			boolean targetCheck = roleCheck(targetEmp.getRoleList(), roles[i]);
			if (targetCheck) returnEmp++;
		}
		log.info("Authenticating User level is {}, Target User level is {}", authEmpLevel, returnEmp);
		return authEmpLevel.compareTo(returnEmp);
	}

	private boolean roleCheck(Collection<Role> roleList, String role) {
		return roleList.stream().anyMatch(e -> e.getName().equals(role));
	}

}
