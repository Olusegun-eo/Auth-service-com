package com.waya.wayaauthenticationservice.assembler;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import com.waya.wayaauthenticationservice.controller.UserController;
import com.waya.wayaauthenticationservice.entity.Users;
import com.waya.wayaauthenticationservice.pojo.UserProfileResponsePojo;

@Component
public class UserAssembler extends RepresentationModelAssemblerSupport<Users, UserProfileResponsePojo>  {

	public UserAssembler() {
		super(UserController.class, UserProfileResponsePojo.class);
	}

	@Override
	public UserProfileResponsePojo toModel(Users entity) {
		UserProfileResponsePojo userDTO = toModelDTO(entity);
		userDTO.add(linkTo(methodOn(UserController.class).findUser(entity.getId())).withSelfRel());
		return userDTO;
	}
	
	@Override
	public CollectionModel<UserProfileResponsePojo> toCollectionModel(Iterable<? extends Users> entities) {
		CollectionModel<UserProfileResponsePojo> userResponseList = super.toCollectionModel(entities);
		return userResponseList;
	}

	private UserProfileResponsePojo toModelDTO(Users user) {
		if (user == null)
			return null;

		Set<String> roles = user.getRolesList().stream().map(u -> u.getName()).collect(Collectors.toSet());
		Set<String> permits = new HashSet<>();
		user.getRolesList().forEach(u -> {
			permits.addAll(u.getPermissions().stream().map(p -> p.getName()).collect(Collectors.toSet()));
		});

		UserProfileResponsePojo userDto = UserProfileResponsePojo.builder().email(user.getEmail()).id(user.getId())
				.isEmailVerified(user.isEmailVerified()).phoneNumber(user.getPhoneNumber())
				.firstName(user.getFirstName()).lastName(user.getSurname()).isAdmin(user.isAdmin())
				.isPhoneVerified(user.isPhoneVerified()).isAccountDeleted(user.isDeleted())
				.isAccountExpired(!user.isAccountNonExpired()).isCredentialsExpired(!user.isCredentialsNonExpired())
				.isActive(user.isActive()).isAccountLocked(!user.isAccountNonLocked()).roles(roles).permits(permits)
				.pinCreated(user.isPinCreated()).isCorporate(user.isCorporate()).build();
		return userDto;
	}
}
