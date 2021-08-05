package com.waya.wayaauthenticationservice.pojo.others;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WalletAccount {

	@JsonProperty("id")
	private Integer id;
	@JsonProperty("del_flg")
	private boolean deleted;
	@JsonProperty("entity_cre_flg")
	private boolean approved;
	@JsonProperty("sol_id")
	private String solId;
	@JsonProperty("bacid")
	private String bacid;
	@JsonProperty("accountNo")
	private String accountNo;
	@JsonProperty("acct_name")
	private String accountName;
	@JsonProperty("gl_code")
	private String glCode;
	@JsonProperty("product_code")
	private String productCode;
	@JsonProperty("acct_ownership")
	private String acctOwnership;
	@JsonProperty("frez_code")
	private String freezeCode;
	@JsonProperty("frez_reason_code")
	private String freezeReasonCode;
	@JsonProperty("acct_opn_date")
	private String acctOpnDate;
	@JsonProperty("acct_cls_flg")
	private boolean acctClsFlg;
	@JsonProperty("clr_bal_amt")
	private BigDecimal clrBalAmt;
	@JsonProperty("un_clr_bal_amt")
	private BigDecimal unClrBalAmt;
	@JsonProperty("hashed_no")
	private String hashedNo;
	@JsonProperty("int_paid_flg")
	private boolean intPaidFlg;
	@JsonProperty("int_coll_flg")
	private boolean intCollFlg;
	@JsonProperty("lchg_user_id")
	private String lchgUserId;
	@JsonProperty("lchg_time")
	private Date lchgTime;
	@JsonProperty("rcre_user_id")
	private String rcreUserId;
	@JsonProperty("rcre_time")
	private String rcreTime;
	@JsonProperty("acct_crncy_code")
	private String acctCrncyCode;
	@JsonProperty("lien_amt")
	private BigDecimal lienAmt;
	@JsonProperty("product_type")
	private String productType;
	@JsonProperty("cum_dr_amt")
	private BigDecimal cumDrAmt;
	@JsonProperty("cum_cr_amt")
	private BigDecimal cumCrAmt;
	@JsonProperty("chq_alwd_flg")
	private boolean chqAlwdFlg;
	@JsonProperty("cash_dr_limit")
	private BigDecimal cashDrLimit;
	@JsonProperty("xfer_dr_limit")
	private BigDecimal xferDrLimit;
	@JsonProperty("cash_cr_limit")
	private BigDecimal cashCrLimit;
	@JsonProperty("xfer_cr_limit")
	private BigDecimal xferCrLimit;
	@JsonProperty("acct_cls_date")
	private String acctClsDate;
	@JsonProperty("last_tran_date")
	private Date lastTranDate;
	@JsonProperty("last_tran_id_dr")
	private String lastTranIdDr;
	@JsonProperty("last_tran_id_cr")
	private String lastTranIdCr;
	@JsonProperty("walletDefault")
	private boolean walletDefault;
	@JsonProperty("lien_reason")
	private String lienReason;

}
