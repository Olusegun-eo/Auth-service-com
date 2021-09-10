package com.waya.wayaauthenticationservice.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.springframework.jdbc.core.RowMapper;

import com.waya.wayaauthenticationservice.entity.Privilege;
import com.waya.wayaauthenticationservice.entity.Role;
import com.waya.wayaauthenticationservice.entity.Users;
import com.waya.wayaauthenticationservice.pojo.userDTO.UserProfilePojo;
import com.waya.wayaauthenticationservice.repository.UserRepository;

public class UserProfilePojoMapper implements RowMapper<UserProfilePojo> {

	
	private UserRepository userRepository;
	
	public UserProfilePojoMapper(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Override
	public UserProfilePojo mapRow(ResultSet rs, int rowNum) throws SQLException {
		Set<String> roles = new HashSet<>();
		Set<String> permits = new HashSet<>();
		UserProfilePojo user = new UserProfilePojo();
		Long userId = rs.getLong("user_id");
		user.setId(rs.getLong("user_id"));
		user.setCreatedAt(rs.getDate("created_at"));
		user.setUpdatedAt(rs.getDate("updated_at"));
		user.setAddress(rs.getString("address"));
		user.setAge(rs.getInt("age"));
		user.setCity(rs.getString("city"));
		user.setDateOfBirth(rs.getDate("date_of_birth"));
		user.setDistrict(rs.getString("district"));
		user.setFirstName(rs.getString("first_name"));
		user.setGender(rs.getString("gender"));
		user.setMiddleName(rs.getString("middle_name"));
		user.setLastName(rs.getString("surname"));
		user.setOrganisationName(rs.getString("organisation_name"));
		user.setReferral(rs.getString("referral"));
		user.setProfileImage(rs.getString("profile_image"));
		user.setState(rs.getString("state"));
		user.setQrCode(rs.getString("qr_code"));
		user.setCreatedBy(rs.getString("created_by"));
		user.setModifiedBy(rs.getString("modified_by"));
		user.setAdmin(rs.getBoolean("is_admin"));
		user.setActive(rs.getBoolean("is_active"));
		user.setEmailVerified(rs.getBoolean("email_verified"));
		user.setPhoneVerified(rs.getBoolean("phone_verified"));
		user.setAccountLocked(rs.getBoolean("account_non_locked"));
		user.setAccountExpired(rs.getBoolean("account_non_expired"));
		user.setCredentialsExpired(rs.getBoolean("account_credentials_non_expired"));
		user.setPinCreated(rs.getBoolean("pin_created"));
		user.setSimulated(rs.getBoolean("is_simulated"));
		user.setIsCorporate(rs.getBoolean("is_corporate"));
		user.setIsDeleted(rs.getBoolean("is_deleted"));
		user.setReferenceCode(rs.getString("reference_code"));
		user.setEmail(rs.getString("email"));
		user.setPhoneNo(rs.getString("phone_number"));
		Optional<Users> mUser = userRepository.findById(userId);
		if (mUser.isPresent()) {
			Collection<Role> role = mUser.get().getRoleList();
			for (Role mRole : role) {
				roles.add(mRole.getName());
			}
			user.setRoles(roles);

			for (Role mRole : role) {
				for (Privilege mPriv : mRole.getPrivileges()) {
					permits.add(mPriv.getName());
				}
			}
			user.setPermits(permits);
		}
		return user;
	}

}
