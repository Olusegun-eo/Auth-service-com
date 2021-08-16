package com.waya.wayaauthenticationservice.assembler;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import com.waya.wayaauthenticationservice.controller.UserController;
import com.waya.wayaauthenticationservice.entity.Users;
import com.waya.wayaauthenticationservice.pojo.userDTO.UserProfileResponsePojo;
import com.waya.wayaauthenticationservice.service.UserService;

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
		return userDTO;
	}
	
	@Override
	public CollectionModel<UserProfileResponsePojo> toCollectionModel(Iterable<? extends Users> entities) {
		CollectionModel<UserProfileResponsePojo> userResponseList = super.toCollectionModel(entities);
		return userResponseList;
	}
}
