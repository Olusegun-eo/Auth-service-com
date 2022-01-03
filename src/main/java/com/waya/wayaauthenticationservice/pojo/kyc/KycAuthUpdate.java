package com.waya.wayaauthenticationservice.pojo.kyc;

import lombok.Data;

@Data
public class KycAuthUpdate {
	
	private Long userId;
	
	private boolean kcyupdate;

	public KycAuthUpdate(Long userId, boolean kcyupdate) {
		super();
		this.userId = userId;
		this.kcyupdate = kcyupdate;
	}

	public KycAuthUpdate() {
		super();
	}
	
	

}
