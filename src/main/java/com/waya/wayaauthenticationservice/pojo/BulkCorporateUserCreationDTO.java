package com.waya.wayaauthenticationservice.pojo;

import java.util.Set;

import lombok.Data;

@Data
public class BulkCorporateUserCreationDTO {

	private Set<CorporateUserPojo> usersList;
}
