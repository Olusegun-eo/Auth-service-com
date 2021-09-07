package com.waya.wayaauthenticationservice.service.impl;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.waya.wayaauthenticationservice.controller.UserController;
import com.waya.wayaauthenticationservice.entity.Privilege;
import com.waya.wayaauthenticationservice.entity.Role;
import com.waya.wayaauthenticationservice.entity.Users;
import com.waya.wayaauthenticationservice.pojo.userDTO.UserProfileResponsePojo;
import com.waya.wayaauthenticationservice.proxy.VirtualAccountProxy;
import com.waya.wayaauthenticationservice.repository.UserRepository;
import com.waya.wayaauthenticationservice.response.ErrorResponse;
import com.waya.wayaauthenticationservice.response.SuccessResponse;
import com.waya.wayaauthenticationservice.service.ProfileService;
import com.waya.wayaauthenticationservice.service.SimulatedService;


@Service
public class SimulatedServiceImpl implements SimulatedService {

	@Autowired
	ProfileService profileService;
	@Autowired
	VirtualAccountProxy virtualAccountProxy;
	@Autowired
	private UserRepository usersRepository;

	@Override
	public ResponseEntity<?> getUserByEmail(String email) {
		Users user = usersRepository.findByEmailIgnoreCase(email).orElse(null);
		if (user == null) {
			return new ResponseEntity<>(new ErrorResponse("Invalid email"), NOT_FOUND);
		} else {
			UserProfileResponsePojo userDto = this.toModelDTO(user);
			return new ResponseEntity<>(new SuccessResponse("User info fetched", userDto), HttpStatus.OK);
		}
	}

	public UserProfileResponsePojo toModelDTO(Users user) {
		if (user == null)
			return null;

		Set<String> roles = user.getRoleList().stream().map(Role::getName).collect(Collectors.toSet());
		Set<String> permits = new HashSet<>();
		user.getRoleList().forEach(
				u -> permits.addAll(u.getPrivileges().stream().map(Privilege::getName).collect(Collectors.toSet())));

		UserProfileResponsePojo userDto = UserProfileResponsePojo.builder().email(user.getEmail()).id(user.getId())
				.referenceCode(user.getReferenceCode()).isEmailVerified(user.isEmailVerified())
				.phoneNumber(user.getPhoneNumber()).firstName(user.getFirstName()).lastName(user.getSurname())
				.isAdmin(user.isAdmin()).isPhoneVerified(user.isPhoneVerified()).isAccountDeleted(user.isDeleted())
				.isAccountExpired(!user.isAccountNonExpired()).isCredentialsExpired(!user.isCredentialsNonExpired())
				.isActive(user.isActive()).isAccountLocked(!user.isAccountNonLocked()).roles(roles).permits(permits)
				.pinCreated(user.isPinCreated()).isCorporate(user.isCorporate()).build();
		userDto.add(linkTo(methodOn(UserController.class).findUser(user.getId())).withSelfRel());

		return userDto;
	}

}
