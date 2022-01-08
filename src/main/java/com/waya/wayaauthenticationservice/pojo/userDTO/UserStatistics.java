package com.waya.wayaauthenticationservice.pojo.userDTO;

import org.springframework.data.domain.Page;

import com.waya.wayaauthenticationservice.entity.UserWallet;

import lombok.Data;

@Data
public class UserStatistics {
	
	private Page<UserWallet> user;
	

}
