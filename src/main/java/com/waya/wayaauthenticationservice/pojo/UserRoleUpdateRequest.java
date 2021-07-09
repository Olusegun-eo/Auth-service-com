package com.waya.wayaauthenticationservice.pojo;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserRoleUpdateRequest {

	private long id;
	private List<Integer> rolesList;
}
