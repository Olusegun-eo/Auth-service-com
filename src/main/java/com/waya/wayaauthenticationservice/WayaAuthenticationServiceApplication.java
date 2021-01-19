package com.waya.wayaauthenticationservice;

import com.waya.wayaauthenticationservice.entity.Roles;
import com.waya.wayaauthenticationservice.repository.RolesRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
@EnableDiscoveryClient
public class WayaAuthenticationServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(WayaAuthenticationServiceApplication.class, args);
	}

	@Bean
	public SpringApplicationContext springApplicationContext() {
		return new SpringApplicationContext();
	}

	@Bean
	public BCryptPasswordEncoder bCryptPasswordEncoder() {
		return new BCryptPasswordEncoder();
	}

//	@Bean
//	public CommandLineRunner demoData(RolesRepository rolesRepo) {
//		return args -> {
//
//			if (rolesRepo.findAll().isEmpty()){
//				Roles roles = new Roles(1, "ADMIN");
//				Roles mRoles = rolesRepo.save(roles);
//				System.out.println("Role Id:::::"+mRoles.getId()+"Role Name::::::"+mRoles.getName());
//				Roles roles1 = new Roles(2, "User");
//				Roles mRoles1 =rolesRepo.save(roles1);
//				System.out.println("Role Id:::::"+mRoles1.getId()+"Role Name::::::"+mRoles1.getName());
//
//
//			}
//
//		};
//	}

}
