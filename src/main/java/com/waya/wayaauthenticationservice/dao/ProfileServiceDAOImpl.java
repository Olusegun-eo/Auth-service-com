package com.waya.wayaauthenticationservice.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Repository;

import com.waya.wayaauthenticationservice.config.DBConnectConfig;

import lombok.extern.slf4j.Slf4j;

@Repository
@Slf4j
public class ProfileServiceDAOImpl implements ProfileServiceDAO {

	@Autowired
	private DBConnectConfig jdbcTemplate;

	@Override
	public Integer getProfileCount(String user_id, String phone) {
		String sql = "SELECT count(*) FROM profile  ";
		sql = sql + "WHERE user_id = ? AND phone_number = ? ";
		int count = 0;
		try {
			Object[] params = new Object[] { user_id, phone };
			count = jdbcTemplate.jdbcConnect().queryForObject(sql, Integer.class, params);
		} catch (EmptyResultDataAccessException ex) {
			log.error(ex.getMessage());
		}
		return count;

	}

}
