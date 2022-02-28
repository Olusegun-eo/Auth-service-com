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
"timeStamp",
"status",
"message",
"data"
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class SettleUserResponse {

	@JsonProperty("timeStamp")
	private String timeStamp;
	
	@JsonProperty("status")
	private boolean status;
	
	@JsonProperty("message")
	private String message;
	
	@JsonProperty("data")
	private SettleData data;

}
