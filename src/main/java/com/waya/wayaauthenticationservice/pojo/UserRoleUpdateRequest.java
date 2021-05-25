package com.waya.wayaauthenticationservice.pojo;

import java.util.List;

import com.waya.wayaauthenticationservice.entity.Roles;

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
