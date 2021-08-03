package com.waya.wayaauthenticationservice.security;

import com.waya.wayaauthenticationservice.entity.Role;
import com.waya.wayaauthenticationservice.entity.Users;
import com.waya.wayaauthenticationservice.enums.ERole;
import com.waya.wayaauthenticationservice.repository.RolesRepository;
import com.waya.wayaauthenticationservice.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component("userSecurity")
@Slf4j
public class UserSecurity {

	private UserRepository userRepo;
	private RolesRepository rolesRepository;

	public UserSecurity(UserRepository userRepo, RolesRepository rolesRepository) {
		this.userRepo = userRepo;
		this.rolesRepository = rolesRepository;
	}

	public boolean useHierarchy(Long id, Authentication authentication) {
		Users user = ((UserPrincipal) authentication.getPrincipal()).getUser()
				.orElse(null);

		if(user == null) return false;

		if (user.getId().equals(id))
			return true;

		Users returnObj = this.userRepo.findById(false, id)
				.orElse(null);

		if(returnObj == null) return true;

		boolean isOga = compareRoles(returnObj, user) > 0;
		log.info("isOga returned {}", isOga);
		return isOga;
	}

	public boolean useHierarchyForRoles(String roleName, Authentication authentication) {
		Users user = ((UserPrincipal) authentication.getPrincipal()).getUser()
				.orElse(null);
		if(user == null) return false;

		Role role = this.rolesRepository.findByName(roleName).orElse(null);
		if(role == null) return true;

		boolean isOga = compareRoles(role, user.getRoleList());
		log.info("isOga returned {}", isOga);
		return isOga;
	}

	public boolean useHierarchy(String emailOrPhoneNumber, Authentication authentication) {

		Users user = ((UserPrincipal) authentication.getPrincipal()).getUser()
				.orElse(null);
		if(user == null) return false;

		if (user.getEmail().equals(emailOrPhoneNumber) ||
				user.getPhoneNumber().equals(emailOrPhoneNumber))
			return true;

		Users returnObj = this.userRepo.findByEmailOrPhoneNumber(emailOrPhoneNumber)
				.orElse(null);
		if(returnObj == null) return true;

		boolean isOga = compareRoles(returnObj, user) > 0;
		log.info("isOga returned {}", isOga);
		return isOga;
	}

	private int compareRoles(Users targetEmp, Users authEmp) {
		String[] roles = ERole.getRoleHierarchy().split(">");

		Integer authEmpLevel = 0;
		Integer returnEmp = 0;

		for (int i = roles.length - 1; i >= 0; i--) {
			boolean authCheck = roleCheck(authEmp.getRoleList(), roles[i].trim());
			if (authCheck) authEmpLevel = roles.length - i;

			boolean targetCheck = roleCheck(targetEmp.getRoleList(), roles[i].trim());
			if (targetCheck) returnEmp = roles.length - i;
		}
		log.info("Authenticating User level is {}, Target User level is {}", authEmpLevel, returnEmp);

		if(authEmpLevel.equals(returnEmp) && authEmpLevel == roles.length)
			return 1;

		return authEmpLevel.compareTo(returnEmp);
	}

	private boolean compareRoles(Role role, Collection<Role> roleList) {
		String[] roles = ERole.getRoleHierarchy().split(">");

		Integer targetRole = 0;
		Integer authEmpLevel = 0;
		boolean hasRole = roleList.contains(role);
		if(hasRole) {
			for(int i = roles.length - 1; i >= 0; i--){
				if(roles[i].trim().equals(role.getName()))
					targetRole = roles.length - i;

				boolean authCheck = roleCheck(roleList, roles[i].trim());
				if (authCheck) authEmpLevel = roles.length - i;
			}
			if(targetRole == roles.length && targetRole.equals(authEmpLevel)) return true;

			return authEmpLevel.compareTo(targetRole) > 0;
		}
		return false;
	}

	private boolean roleCheck(Collection<Role> roleList, String role) {
		return roleList.stream().anyMatch(e -> e.getName().equals(role));
	}

}
