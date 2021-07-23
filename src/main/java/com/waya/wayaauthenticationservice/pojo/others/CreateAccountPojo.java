package com.waya.wayaauthenticationservice.pojo.others;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateAccountPojo {

	private Long userId;
	private BigDecimal custDebitLimit;
	private LocalDateTime custExpIssueDate;
	private String custIssueId;
	private String custSex;
	private String custTitleCode;
    private LocalDate dob;
    private String emailId;
	private String firstName;
    private String lastName;
    private String solId;
    private String mobileNo;
}
