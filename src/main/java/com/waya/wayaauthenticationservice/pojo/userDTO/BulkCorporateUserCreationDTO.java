package com.waya.wayaauthenticationservice.pojo.userDTO;

import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BulkCorporateUserCreationDTO {

	private Set<CorporateUserPojo> usersList;
}
