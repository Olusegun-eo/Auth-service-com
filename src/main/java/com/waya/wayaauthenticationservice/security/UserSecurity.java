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

import static com.waya.wayaauthenticationservice.util.HelperUtils.emailPattern;

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
		Users user = ((UserPrincipal) authentication.getPrincipal()).getUser().orElse(null);

		if (user == null)
			return false;

		if (user.getId().equals(id)){
			log.info("Same User request");
			return true;
		}

		Users returnObj = this.userRepo.findById(false, id).orElse(null);

		if (returnObj == null)
			return true;

		boolean isOga = compareRoles(returnObj, user) > 0;
		log.info("isOga returned {}", isOga);
		return isOga;
	}

	public boolean useHierarchyForRoles(String roleName, Authentication authentication) {
		Users user = ((UserPrincipal) authentication.getPrincipal()).getUser().orElse(null);
		if (user == null)
			return false;

		Role role = this.rolesRepository.findByName(roleName).orElse(null);
		if (role == null)
			return true;

		boolean isOga = compareRoles(role, user.getRoleList());
		log.info("isOga returned {}", isOga);
		return isOga;
	}

	public boolean useHierarchy(String emailOrPhoneNumber, Authentication authentication) {

		Users user = ((UserPrincipal) authentication.getPrincipal()).getUser().orElse(null);
		if (user == null)
			return false;

		String principal = emailOrPhoneNumber.replaceAll("\\s+", "").trim();
		boolean isEmail = emailPattern.matcher(principal).matches();
		if (!isEmail) {
			if (principal.startsWith("+")) {
				principal = principal.substring(1);
			}
			if (principal.length() > 10) {
				principal = principal.substring(principal.length() - 10);
			}
		}
		
		String userPhone = user.getPhoneNumber();
		if (userPhone != null && userPhone.length() > 10) {
			userPhone = userPhone.substring(userPhone.length() - 10);
		}
		if (user.getEmail().equals(principal) || userPhone.equals(principal)){
			log.info("Same User request");
			return true;
		}

		Users returnObj = this.userRepo.findByEmailOrPhoneNumber(principal).orElse(null);
		if (returnObj == null)
			return true;

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
			if (authCheck)
				authEmpLevel = roles.length - i;

			boolean targetCheck = roleCheck(targetEmp.getRoleList(), roles[i].trim());
			if (targetCheck)
				returnEmp = roles.length - i;
		}
		log.info("Authenticating User level is {}, Target User level is {}", authEmpLevel, returnEmp);

		if (authEmpLevel.equals(returnEmp) && authEmpLevel == roles.length)
			return 1;

		return authEmpLevel.compareTo(returnEmp);
	}

	private boolean compareRoles(Role role, Collection<Role> roleList) {
		String[] roles = ERole.getRoleHierarchy().split(">");

		Integer targetRole = 0;
		Integer authEmpLevel = 0;
		boolean hasRole = roleList.contains(role);
		if (hasRole) {
			for (int i = roles.length - 1; i >= 0; i--) {
				if (roles[i].trim().equals(role.getName()))
					targetRole = roles.length - i;

				boolean authCheck = roleCheck(roleList, roles[i].trim());
				if (authCheck)
					authEmpLevel = roles.length - i;
			}
			if (targetRole == roles.length && targetRole.equals(authEmpLevel))
				return true;

			return authEmpLevel.compareTo(targetRole) > 0;
		}
		return false;
	}

	private boolean roleCheck(Collection<Role> roleList, String role) {
		return roleList.stream().anyMatch(e -> e.getName().equals(role));
	}

}
