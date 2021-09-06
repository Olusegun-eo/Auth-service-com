package com.waya.wayaauthenticationservice.pojo.userDTO;

import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BulkPrivateUserCreationDTO {

	@NotEmpty(message= "List Should Not be Empty")
	private Set<@Valid BaseUserPojo> usersList;
	
	
}
