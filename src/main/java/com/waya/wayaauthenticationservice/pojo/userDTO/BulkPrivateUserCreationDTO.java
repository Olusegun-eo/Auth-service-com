package com.waya.wayaauthenticationservice.pojo.userDTO;

import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BulkPrivateUserCreationDTO {

	private Set<BaseUserPojo> usersList;
	
}
