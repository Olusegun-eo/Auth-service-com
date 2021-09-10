package com.waya.wayaauthenticationservice.dao;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.waya.wayaauthenticationservice.config.DBConnectConfig;
import com.waya.wayaauthenticationservice.mapper.UserProfilePojoMapper;
import com.waya.wayaauthenticationservice.pojo.userDTO.UserProfilePojo;
import com.waya.wayaauthenticationservice.repository.UserRepository;

import lombok.extern.slf4j.Slf4j;

@Repository
@Slf4j
public class ProfileServiceDAOImpl implements ProfileServiceDAO {

	@Autowired
	private DBConnectConfig jdbcTTemplate;
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	private UserRepository userRepository;
	
	public ProfileServiceDAOImpl(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Override
	public Integer getProfileCount(String user_id, String phone) {
		String sql = "SELECT count(*) FROM m_user_profile  ";
		sql = sql + "WHERE user_id = ? AND phone_number = ? ";
		int count = 0;
		try {
			Object[] params = new Object[] { user_id, phone };
			count = jdbcTTemplate.jdbcConnect().queryForObject(sql, Integer.class, params);
		} catch (EmptyResultDataAccessException ex) {
			log.error(ex.getMessage());
		}
		return count;

	}
	
	public List<UserProfilePojo> GetAllUserProfile() {
		StringBuilder query = new StringBuilder();
		List<UserProfilePojo> userList = new ArrayList<>();
		query.append("SELECT user_id,p.created_at,p.updated_at,address,age,city,date_of_birth,district,");
		query.append("p.first_name,gender,middle_name,p.surname,organisation_name,referral,profile_image,");
		query.append("state,qr_code,p.created_by,p.modified_by,is_admin,is_active,email_verified,phone_verified,");
		query.append("account_non_locked,account_non_expired,account_credentials_non_expired,pin_created,");
		query.append("is_simulated,is_corporate,is_deleted,reference_code,pl.email,pl.phone_number  ");
		query.append("FROM m_user_profile p JOIN m_users pl ON p.user_id = cast(pl.id AS VARCHAR) ");
		query.append("AND pl.is_simulated = false AND pl.is_deleted =false AND p.deleted =false  ");
		query.append("Order by p.created_at desc");
		String sql = query.toString();
		try {
			UserProfilePojoMapper rowMapper = new UserProfilePojoMapper(userRepository);
			userList = jdbcTemplate.query(sql, rowMapper);
			return userList;
		} catch (Exception ex) {
			log.error("An error Occured: Cause: {} \r\n Message: {}", ex.getCause(), ex.getMessage());
			return null;
		}
	}
	
	public UserProfilePojo GetUserProfile(Long user_id) {
		String userId = user_id.toString();
		UserProfilePojo user = new UserProfilePojo();
		StringBuilder query = new StringBuilder();
		query.append("SELECT user_id,p.created_at,p.updated_at,address,age,city,date_of_birth,district,");
		query.append("p.first_name,gender,middle_name,p.surname,organisation_name,referral,profile_image,");
		query.append("state,qr_code,p.created_by,p.modified_by,is_admin,is_active,email_verified,phone_verified,");
		query.append("account_non_locked,account_non_expired,account_credentials_non_expired,pin_created,");
		query.append("is_simulated,is_corporate,is_deleted,reference_code,pl.email,pl.phone_number  ");
		query.append("FROM m_user_profile p JOIN m_users pl ON p.user_id = cast(pl.id AS VARCHAR) ");
		query.append("AND pl.is_simulated = false AND pl.is_deleted =false AND p.deleted =false  ");
		query.append("AND p.user_id = ? order by p.created_at desc");
		String sql = query.toString();
		try {
			Object[] params = new Object[] { userId };
			UserProfilePojoMapper rowMapper = new UserProfilePojoMapper(userRepository);
			user = jdbcTemplate.queryForObject(sql, rowMapper, params);
		} catch (Exception ex) {
			log.error(ex.getMessage());
		}
		return user;
	}
	
	public List<UserProfilePojo> GetAllSimulatedUserProfile() {
		StringBuilder query = new StringBuilder();
		List<UserProfilePojo> userList = new ArrayList<>();
		query.append("SELECT user_id,p.created_at,p.updated_at,address,age,city,date_of_birth,district,");
		query.append("p.first_name,gender,middle_name,p.surname,organisation_name,referral,profile_image,");
		query.append("state,qr_code,p.created_by,p.modified_by,is_admin,is_active,email_verified,phone_verified,");
		query.append("account_non_locked,account_non_expired,account_credentials_non_expired,pin_created,");
		query.append("is_simulated,is_corporate,is_deleted,reference_code,pl.email,pl.phone_number  ");
		query.append("FROM m_user_profile p JOIN m_users pl ON p.user_id = cast(pl.id AS VARCHAR) ");
		query.append("AND pl.is_simulated = true AND pl.is_deleted =false AND p.deleted =false  ");
		query.append("Order by p.created_at desc");
		String sql = query.toString();
		try {
			UserProfilePojoMapper rowMapper = new UserProfilePojoMapper(userRepository);
			userList = jdbcTemplate.query(sql, rowMapper);
			return userList;
		} catch (Exception ex) {
			log.error("An error Occured: Cause: {} \r\n Message: {}", ex.getCause(), ex.getMessage());
			return null;
		}
	}
	
	public UserProfilePojo GetSimulatedUserProfile(Long user_id) {
		String userId = user_id.toString();
		UserProfilePojo user = new UserProfilePojo();
		StringBuilder query = new StringBuilder();
		query.append("SELECT user_id,p.created_at,p.updated_at,address,age,city,date_of_birth,district,");
		query.append("p.first_name,gender,middle_name,p.surname,organisation_name,referral,profile_image,");
		query.append("state,qr_code,p.created_by,p.modified_by,is_admin,is_active,email_verified,phone_verified,");
		query.append("account_non_locked,account_non_expired,account_credentials_non_expired,pin_created,");
		query.append("is_simulated,is_corporate,is_deleted,reference_code,pl.email,pl.phone_number  ");
		query.append("FROM m_user_profile p JOIN m_users pl ON p.user_id = cast(pl.id AS VARCHAR) ");
		query.append("AND pl.is_simulated = true AND pl.is_deleted =false AND p.deleted =false  ");
		query.append("AND p.user_id = ? order by p.created_at desc");
		String sql = query.toString();
		try {
			Object[] params = new Object[] { userId };
			UserProfilePojoMapper rowMapper = new UserProfilePojoMapper(userRepository);
			user = jdbcTemplate.queryForObject(sql, rowMapper, params);
		} catch (Exception ex) {
			log.error(ex.getMessage());
		}
		return user;
	}

}
