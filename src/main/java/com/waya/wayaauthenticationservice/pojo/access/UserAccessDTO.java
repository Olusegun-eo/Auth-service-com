package com.waya.wayaauthenticationservice.pojo.access;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserAccessDTO {

	private Long userId;

	private String name;

	private String phoneNumber;

	private String emailAddress;

	private Long roleId;

}
