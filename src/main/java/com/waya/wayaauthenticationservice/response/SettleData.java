package com.waya.wayaauthenticationservice.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
"id",
"userId",
"fullName",
"phoneNo",
"email",
"status",
"usertype",
"createdDate",
"deleted"
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class SettleData {
	
	@JsonProperty("id")
	private long id;
	
	@JsonProperty("userId")	
	private long userId;
	
	@JsonProperty("fullName")
	private String fullName;
	
	@JsonProperty("phoneNo")
	private String phoneNo;
	
	@JsonProperty("email")
	private String email;
	
	@JsonProperty("status")
	private String status;
	
	@JsonProperty("usertype")
	private String usertype;
	
	@JsonProperty("createdDate")
	private String createdDate;
	
	@JsonProperty("deleted")
	private boolean deleted;

}
