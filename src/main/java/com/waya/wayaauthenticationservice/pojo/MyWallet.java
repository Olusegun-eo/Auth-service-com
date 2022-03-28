package com.waya.wayaauthenticationservice.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Data
public class MyWallet {
	
	private Long id;
    private boolean del_flg;
    private String  sol_id;
    private String accountNo;
    private String acct_name;
    private String acct_cls_flg;
    private Double clr_bal_amt;

}
