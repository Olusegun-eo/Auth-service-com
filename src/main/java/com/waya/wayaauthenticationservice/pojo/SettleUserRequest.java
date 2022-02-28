package com.waya.wayaauthenticationservice.pojo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Data;
import lombok.ToString;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
"createAt",
"email",
"fullName",
"phoneNo",
"status",
"userId",
"usertype"
})
@Data
@ToString
public class SettleUserRequest {
	
	@JsonProperty("createAt")
	private String createAt;
	
	@JsonProperty("email")
	private String email;
	
	@JsonProperty("fullName")
	private String fullName;
	
	@JsonProperty("phoneNo")
	private String phoneNo;
	
	@JsonProperty("status")
	private String status;
	
	@JsonProperty("userId")
	private long userId;
	
	@JsonProperty("usertype")
	private String usertype;

	public SettleUserRequest(String createAt, String email, String fullName, String phoneNo, String status,
			long userId, String usertype) {
		super();
		this.createAt = createAt;
		this.email = email;
		this.fullName = fullName;
		this.phoneNo = phoneNo;
		this.status = status;
		this.userId = userId;
		this.usertype = usertype;
	}

	public SettleUserRequest() {
		super();
	}
	

}
