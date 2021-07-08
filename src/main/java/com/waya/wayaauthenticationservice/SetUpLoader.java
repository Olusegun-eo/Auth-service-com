//package com.waya.wayaauthenticationservice;
//
//import java.util.Arrays;
//import java.util.Collection;
//import java.util.List;
//import java.util.Optional;
//
//import javax.transaction.Transactional;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.ApplicationListener;
//import org.springframework.context.event.ContextRefreshedEvent;
//import org.springframework.stereotype.Component;
//
//import com.waya.wayaauthenticationservice.entity.Privilege;
//import com.waya.wayaauthenticationservice.entity.Roles;
//import com.waya.wayaauthenticationservice.repository.PrivilegeRepository;
//import com.waya.wayaauthenticationservice.repository.RolesRepository;
//
//@Component
//public class SetUpLoader implements ApplicationListener<ContextRefreshedEvent> {
//
//	boolean alreadySetup = false;
//
//	@Autowired
//	private RolesRepository roleRepository;
//
//	@Autowired
//	private PrivilegeRepository privilegeRepository;
//
//	@Override
//	@Transactional
//	public void onApplicationEvent(ContextRefreshedEvent event) {
//
//		if (alreadySetup)
//			return;
//
//		Privilege readPrivilege = createPrivilegeIfNotFound("READ_USER");
//		Privilege updatePrivilege = createPrivilegeIfNotFound("UPDATE_USER");
//		Privilege writePrivilege = createPrivilegeIfNotFound("CREATE_USER");
//		Privilege lockPrivilege = createPrivilegeIfNotFound("LOCK_USER");
//		Privilege unlockPrivilege = createPrivilegeIfNotFound("UNLOCK_USER");
//		Privilege deletePrivilege = createPrivilegeIfNotFound("DELETE_USER");
//
//		List<Privilege> userPrivileges = Arrays.asList(readPrivilege);
//		List<Privilege> adminPrivileges = Arrays.asList(readPrivilege, updatePrivilege, writePrivilege, lockPrivilege,
//				unlockPrivilege);
//		List<Privilege> mngrPrivileges = Arrays.asList(readPrivilege, updatePrivilege, writePrivilege, lockPrivilege,
//				unlockPrivilege, deletePrivilege);
//
//		createRoleIfNotFound("ROLE_USER", userPrivileges);
//		createRoleIfNotFound("ROLE_ADMIN", adminPrivileges);
//		createRoleIfNotFound("ROLE_SUPER", mngrPrivileges);
//		createRoleIfNotFound("ROLE_MERCH", mngrPrivileges);
//		alreadySetup = true;
//	}
//
//	@Transactional
//	Privilege createPrivilegeIfNotFound(String name) {
//		Optional<Privilege> option = privilegeRepository.findByName(name);
//		Privilege privilege = new Privilege(name);
//		if (!option.isPresent()) {
//			privilege = privilegeRepository.save(privilege);
//		}
//		return privilege;
//	}
//
//	@Transactional
//	Roles createRoleIfNotFound(String name, Collection<Privilege> privileges) {
//		Optional<Roles> option = roleRepository.findByName(name);
//		Roles role = new Roles(name);
//		role.setPermissions(privileges);
//		if (!option.isPresent()) {
//			role = roleRepository.save(role);
//		}
//		return role;
//	}
//}