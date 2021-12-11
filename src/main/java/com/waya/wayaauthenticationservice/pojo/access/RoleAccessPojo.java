package com.waya.wayaauthenticationservice.pojo.access;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RoleAccessPojo {
	
	private Long id;
    private String name;
    private String description;
    private boolean disabled;    

}
