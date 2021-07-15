package com.waya.wayaauthenticationservice;

import java.util.*;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import com.waya.wayaauthenticationservice.entity.Privilege;
import com.waya.wayaauthenticationservice.entity.Roles;
import com.waya.wayaauthenticationservice.repository.PrivilegeRepository;
import com.waya.wayaauthenticationservice.repository.RolesRepository;

@Component
public class SetUpLoader implements ApplicationListener<ContextRefreshedEvent> {

	boolean alreadySetup = false;

	@Autowired
	private RolesRepository roleRepository;

	@Autowired
	private PrivilegeRepository privilegeRepository;

	@Override
	@Transactional
	public void onApplicationEvent(ContextRefreshedEvent event) {

		if (alreadySetup)
			return;

		Privilege readPrivilege = createPrivilegeIfNotFound("READ_USER", "READ");
		Privilege updatePrivilege = createPrivilegeIfNotFound("UPDATE_USER", "UPDATE");
		Privilege writePrivilege = createPrivilegeIfNotFound("CREATE_USER", "CREATE");
		Privilege lockPrivilege = createPrivilegeIfNotFound("LOCK_USER", "LOCK");
		Privilege unlockPrivilege = createPrivilegeIfNotFound("UNLOCK_USER", "UNLOCK");
		Privilege deletePrivilege = createPrivilegeIfNotFound("DELETE_USER", "DELETE");
        Privilege appAdmin = createPrivilegeIfNotFound("APP_ADMIN", "ADMIN");
        Privilege appOwner = createPrivilegeIfNotFound("APP_OWNER", "OWNER");

		List<Privilege> userPrivileges = Collections.singletonList(readPrivilege);
        List<Privilege> merchAdminPrivileges = Arrays.asList(readPrivilege, updatePrivilege, writePrivilege, lockPrivilege,
                unlockPrivilege);
        List<Privilege> adminPrivileges = Arrays.asList(readPrivilege, updatePrivilege, writePrivilege, lockPrivilege,
                unlockPrivilege, deletePrivilege, appAdmin);
		List<Privilege> ownerPrivileges = Arrays.asList(readPrivilege, updatePrivilege, writePrivilege, lockPrivilege,
				unlockPrivilege, deletePrivilege, appOwner);

		createRoleIfNotFound("ROLE_USER", "USER ROLE", userPrivileges);
        createRoleIfNotFound("ROLE_CORP", "CORPORATE ROLE", userPrivileges);
        createRoleIfNotFound("ROLE_CORP_ADMIN", "CORPORATE ADMIN ROLE", merchAdminPrivileges);
		createRoleIfNotFound("ROLE_APP_ADMIN", "APPLICATION ADMIN", adminPrivileges);
		createRoleIfNotFound("ROLE_SUPER_ADMIN", "SUPER ADMIN ROLE", ownerPrivileges);

		alreadySetup = true;
	}

	@Transactional
	Privilege createPrivilegeIfNotFound(String name, String description) {
		Optional<Privilege> option = privilegeRepository.findByName(name);
		Privilege privilege = new Privilege(name);
		privilege.setDescription(description);
		if (!option.isPresent()) {
			privilege = privilegeRepository.save(privilege);
		}
		return privilege;
	}

	@Transactional
	Roles createRoleIfNotFound(String name, String description, Collection<Privilege> privileges) {
		Optional<Roles> option = roleRepository.findByName(name);
		Roles role = new Roles(name);
		role.setDescription(description);
		role.setPermissions(privileges);
		if (!option.isPresent()) {
			role = roleRepository.save(role);
		}
		return role;
	}
}
