package com.waya.wayaauthenticationservice.pojo.userDTO;

import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BulkCorporateUserCreationDTO {

	@NotEmpty(message= "List Should Not be Empty")
	private Set<@Valid CorporateUserPojo> usersList;
}
