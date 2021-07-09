package com.waya.wayaauthenticationservice.pojo;

import java.math.BigDecimal;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WalletAccount {

	private Long id;
	private String accountNo;
	private String accountName;
	private BigDecimal balance;
	private BigDecimal lagerBalance;
	private boolean active;
	private boolean closed;
	private boolean approved;
	private boolean rejected;
	private boolean setSubmittedAndPendingApproval;
	private boolean withdrawnByApplicant;
	private String code;
	private String value;
	private Long productId;
	private String accountType;

	@JsonFormat(pattern = "yyyy-MM-dd", shape = JsonFormat.Shape.STRING)
	private Date createdAt;

	@JsonFormat(pattern = "yyyy-MM-dd", shape = JsonFormat.Shape.STRING)
	private Date updatedAt;

	@JsonProperty("default")
	private boolean isdefault;
}
