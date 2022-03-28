package com.waya.wayaauthenticationservice.pojo;

import java.util.Date;
import java.util.List;

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
public class WalletResponse {
	
	private Date timeStamp;
	private boolean status;
	private String message;
	private List<MyWallet> data;

}
