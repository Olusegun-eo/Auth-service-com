package com.waya.wayaauthenticationservice.assembler;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import com.waya.wayaauthenticationservice.service.UserService;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import com.waya.wayaauthenticationservice.controller.UserController;
import com.waya.wayaauthenticationservice.entity.Users;
import com.waya.wayaauthenticationservice.pojo.UserProfileResponsePojo;

@Component
public class UserAssembler extends RepresentationModelAssemblerSupport<Users, UserProfileResponsePojo>  {

	private UserService userService;

	public UserAssembler(UserService userService) {
		super(UserController.class, UserProfileResponsePojo.class);
		this.userService = userService;
	}

	@Override
	public UserProfileResponsePojo toModel(Users entity) {
		UserProfileResponsePojo userDTO = this.userService.toModelDTO(entity);
		userDTO.add(linkTo(methodOn(UserController.class).findUser(entity.getId())).withSelfRel());
		return userDTO;
	}
	
	@Override
	public CollectionModel<UserProfileResponsePojo> toCollectionModel(Iterable<? extends Users> entities) {
		CollectionModel<UserProfileResponsePojo> userResponseList = super.toCollectionModel(entities);
		return userResponseList;
	}
}
