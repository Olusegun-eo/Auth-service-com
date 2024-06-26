package com.waya.wayaauthenticationservice.pojo.others;

import com.waya.wayaauthenticationservice.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserEditPojo {
	
	private long id;
    private String email;
    private String phoneNumber;
    private String referenceCode;
    private String firstName;
    private String surname;
    private boolean phoneVerified = false;
    private boolean emailVerified = false;
    private boolean pinCreated = false;
    private boolean isCorporate = false;
    private List<Role> roleList;

}
