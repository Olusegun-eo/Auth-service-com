package com.waya.wayaauthenticationservice;

import com.waya.wayaauthenticationservice.entity.BusinessType;
import com.waya.wayaauthenticationservice.entity.Privilege;
import com.waya.wayaauthenticationservice.entity.Role;
import com.waya.wayaauthenticationservice.enums.ERole;
import com.waya.wayaauthenticationservice.repository.BusinessTypeRepository;
import com.waya.wayaauthenticationservice.repository.PrivilegeRepository;
import com.waya.wayaauthenticationservice.repository.RolesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.*;

@Component
public class SetUpLoader implements ApplicationListener<ContextRefreshedEvent> {

	boolean alreadySetup = false;

	@Autowired
	private RolesRepository roleRepository;

	@Autowired
	private BusinessTypeRepository bizRepository;

	@Autowired
	private PrivilegeRepository privilegeRepository;

	@Override
	@Transactional
	public void onApplicationEvent(ContextRefreshedEvent event) {

		if (alreadySetup)
			return;

		List<String> businessTypes = Arrays.asList("Catering and Food", "Cakes and Pastries",
		"Event Planning", "Music and DJ", "Event Courier and Logistics", "Health and Skin Care",
		"Fashion", "Clothing, Accessories, and Shoes", "Makeup", "Hairs", "Computer, Accessories, and Services",
		"Babies and Kids", "Art, Crafts, and Collectibles", "Home and Gardens", "Groceries", "Transportation",
		"Pharmacy/Hospitals", "Aggregator", "Agent", "Agency banking", "Financial institution", "Church",
		"Mosque", "School", "Supermarket", "E-Commerce", "Consulting");
		for(String type : businessTypes){
			if(!bizRepository.existsByBusinessTypeIgnoreCase(type)){
				bizRepository.save(new BusinessType(type));
			}
		}

		Privilege readPrivilege = createPrivilegeIfNotFound("READ_USER", "READ");
		Privilege updatePrivilege = createPrivilegeIfNotFound("UPDATE_USER", "UPDATE");
		Privilege writePrivilege = createPrivilegeIfNotFound("CREATE_USER", "CREATE");
		Privilege lockPrivilege = createPrivilegeIfNotFound("LOCK_USER", "LOCK");
		Privilege unlockPrivilege = createPrivilegeIfNotFound("UNLOCK_USER", "UNLOCK");
		Privilege deletePrivilege = createPrivilegeIfNotFound("DELETE_USER", "DELETE");
        Privilege appAdmin = createPrivilegeIfNotFound("APP_ADMIN", "ADMIN");
		Privilege appSuperAdmin = createPrivilegeIfNotFound("APP_SUPER_ADMIN", "SUPER ADMIN");
        Privilege appOwner = createPrivilegeIfNotFound("APP_OWNER", "OWNER");
        Privilege appMerchant = createPrivilegeIfNotFound("APP_MERCHANT","MERCHANT");
        Privilege appAggregator = createPrivilegeIfNotFound("APP_AGGREGATOR","AGGREGATOR");
        Privilege appAgent = createPrivilegeIfNotFound("APP_AGENT","AGENT");

		List<Privilege> userPrivileges = Collections.singletonList(readPrivilege);
        List<Privilege> merchAdminPrivileges = Arrays.asList(readPrivilege, updatePrivilege, writePrivilege, lockPrivilege,
                unlockPrivilege,appMerchant,appAggregator,appAgent);
        List<Privilege> adminPrivileges = Arrays.asList(readPrivilege, updatePrivilege, writePrivilege, lockPrivilege,
                unlockPrivilege, deletePrivilege, appAdmin,appMerchant,appAggregator,appAgent);
		List<Privilege> superAdminPrivileges = Arrays.asList(readPrivilege, updatePrivilege, writePrivilege, lockPrivilege,
				unlockPrivilege, deletePrivilege, appAdmin, appSuperAdmin,appMerchant,appAggregator,appAgent);
		List<Privilege> ownerPrivileges = Arrays.asList(readPrivilege, updatePrivilege, writePrivilege, lockPrivilege,
				unlockPrivilege, deletePrivilege,appAdmin, appSuperAdmin, appOwner,appMerchant,appAggregator,appAgent);

		createRoleIfNotFound(ERole.ROLE_USER.name(), "USER ROLE", userPrivileges);
        createRoleIfNotFound(ERole.ROLE_CORP.name(), "CORPORATE USER ROLE", userPrivileges);
        createRoleIfNotFound(ERole.ROLE_CORP_ADMIN.name(), "CORPORATE ADMIN ROLE", merchAdminPrivileges);
		createRoleIfNotFound(ERole.ROLE_APP_ADMIN.name(), "APPLICATION ADMIN", adminPrivileges);
		createRoleIfNotFound(ERole.ROLE_SUPER_ADMIN.name(), "SUPER ADMIN ROLE", superAdminPrivileges);
		createRoleIfNotFound(ERole.ROLE_OWNER_ADMIN.name(), "OWNER ADMIN ROLE", ownerPrivileges);
		//createRoleIfNotFound(ERole.ROLE_AGENT.name(),"BIZ_AGENT", userPrivileges);
		//createRoleIfNotFound(ERole.ROLE_AGGREGATOR.name(), "BIZ_AGGREGATOR", userPrivileges);
		//createRoleIfNotFound(ERole.ROLE_MERCHANT.name(), "BIZ_MERCHANT", userPrivileges);

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
	Role createRoleIfNotFound(String name, String description, Collection<Privilege> privileges) {
		Optional<Role> option = roleRepository.findByName(name);
		Role role = new Role(name);
		role.setDescription(description);
		role.setPrivileges(privileges);
		if (!option.isPresent()) {
			role = roleRepository.save(role);
		}
		return role;
	}
}
