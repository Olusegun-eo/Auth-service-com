package com.waya.wayaauthenticationservice.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;

import com.waya.wayaauthenticationservice.model.AuthProvider;

import lombok.Data;

@Data
@Entity
@Table(name = "m_users_backup")
public class UsersBackUp implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	private Long id;

	private String email;

	private String phoneNumber;

	private String referenceCode;

	private String firstName;

	private String surname;

	private String password;

	private String pinHash;

	private String name;

	private boolean phoneVerified = false;

	private boolean emailVerified = false;

	@Column(name = "email_verified_date")
	private LocalDateTime emailVerifiedDate;

	private boolean pinCreated = false;

	private boolean isCorporate = false;

	private boolean isAdmin = false;

	private AuthProvider provider;

	private String providerId;

	private String regDeviceType;

	private String regDevicePlatform;

	private String regDeviceIP;

	@Lob
	private String imageUrl;

	@Column(name = "account_non_expired")
	private boolean accountNonExpired;

	@Column(name = "account_expired_date")
	private LocalDateTime accountExpiredDate;

	@Column(name = "account_non_locked")
	private boolean accountNonLocked =  false;

	@Column(name = "account_lock_date")
	private LocalDateTime accountLockDate;

	@Column(name = "account_credentials_non_expired")
	private boolean credentialsNonExpired;

	@Column(name = "credential_expired_date")
	private LocalDateTime credentialExpiredDate;

	@Column(name = "is_active")
	private boolean isActive = false;

	@Column(name = "first_time_login_remaining")
	private boolean firstTimeloginRemaining;

	@Column(name = "first_time_login_date")
	private LocalDateTime firstTimeloginDate;

	@Column(name = "is_deleted")
	private boolean deleted;

	@Column(name = "last_time_password_updated")
	private LocalDateTime lastTimePasswordUpdated;

	@Column(name = "password_never_expires", nullable = false)
	private boolean passwordNeverExpires;

	@ElementCollection
	private List<String> rolesList = new ArrayList<>();

	@CreationTimestamp
	private LocalDateTime dateCreated;

	private LocalDateTime pinCreatedDate;

	private LocalDateTime dateOfActivation;

	//@Column(name = "user_id", unique = true, nullable = false)
	@Column(name = "user_id", nullable = false)
	private String userId;

	public UsersBackUp() {
		provider = AuthProvider.local;
		this.accountNonLocked = true;
		this.credentialsNonExpired = true;
		this.accountNonExpired = true;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof UsersBackUp)) {
			return false;
		}
		UsersBackUp other = (UsersBackUp) obj;
		return Objects.equals(email, other.email) && id == other.id && Objects.equals(phoneNumber, other.phoneNumber)
				&& Objects.equals(surname, other.surname);
	}

	@Override
	public int hashCode() {
		return Objects.hash(email, id, phoneNumber, surname);
	}
	
}