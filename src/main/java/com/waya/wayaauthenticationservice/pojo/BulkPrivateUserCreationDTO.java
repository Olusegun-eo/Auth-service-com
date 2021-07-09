package com.waya.wayaauthenticationservice.pojo;

import java.util.Set;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BulkPrivateUserCreationDTO {

	private Set<UserPojo> usersList;
	
}
