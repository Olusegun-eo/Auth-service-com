package com.waya.wayaauthenticationservice.pojo.others;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreateAccountPojo {

	private Long userId;
	private BigDecimal custDebitLimit;
	private String custExpIssueDate;
	private String custIssueId;
	private String custSex;
	private String custTitleCode;
    private String dob;
    private String emailId;
	private String firstName;
    private String lastName;
    private String solId;
    private String mobileNo;
}
