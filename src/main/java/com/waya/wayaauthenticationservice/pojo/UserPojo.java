package com.waya.wayaauthenticationservice.pojo;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserPojo {

	@NotNull(message = "Email Cannot be Null")
	@Pattern(regexp = "[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\." + "[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@"
			+ "(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?", message = "Invalid Email")
	private String email;
	
	@Size(min = 10, message = "Currency Code must be 3 characters")
	private String phoneNumber;
	
	@NotNull(message = "Reference Code Cannot be Null")
	@Size(min = 3, message = "Reference Code must be at least 3 characters")
	private String referenceCode;
	
	@NotNull(message = "First Name Cannot be Null")
	@Size(min = 2, message = "First Name must be at least 2 characters")
	private String firstName;
	
	@NotNull(message = "Last Name Cannot be Null")
	@Size(min = 2, message = "Last Name must be at least 2 characters")
	private String surname;
	
	@NotNull(message = "Password Cannot be null")
	@Size(min = 8, message = "Password must be at least 8 characters")
	private String password;
	
	@NotNull(message = "isAdmin Cannot be null")
	private boolean isAdmin = false;
}
