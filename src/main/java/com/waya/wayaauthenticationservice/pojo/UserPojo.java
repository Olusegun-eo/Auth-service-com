package com.waya.wayaauthenticationservice.pojo;

import javax.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserPojo extends BaseUserPojo{

	@NotNull(message = "isAdmin Cannot be null")
	private boolean isAdmin = false;
}
