package com.waya.wayaauthenticationservice.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.validator.constraints.Length;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@Entity
@Table(name = "m_users")
public class Users implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    private long id;

    @Email(message = "email should be valid")
    @Column(unique = true)
    private String email;

    @NotNull(message = "phone number cannot be null")
    private String phoneNumber;

    private String referenceCode;
    @NotBlank(message = "first Name cannot be null")

    private String firstName;

    @NotBlank(message = "last Name cannot be null")
    private String surname;


    @JsonIgnore
    @NotBlank(message = "password cannot be null")
    @Length(min = 8, max = 100, message = "password must be greater than 8 characters")
    private String password;

    @JsonIgnore
    private int pin;

    @JsonIgnore
    private boolean phoneVerified = false;

    @JsonIgnore
    private boolean emailVerified = false;
    
    @Column(name = "email_verified_date")
    private LocalDateTime emailVerifiededDate;

    private boolean pinCreated = false;

    private boolean isCorporate = false;
    
    private boolean isAdmin = false;
    
    private LocalDateTime dateOfInactive;

    @Transient
    private Roles role;
    
    @NotNull
    @Enumerated(EnumType.STRING)
    private AuthProvider provider;

    private String providerId;
    
    private String regDeviceType;
    
    private String regDevicePlatform;
    
    private String regDeviceIP;
    
    private String imageUrl;
    
    @Column(name = "account_non_expired", nullable = false)
	private boolean accountNonExpired;
    
    @Column(name = "account_expired_date")
	private LocalDateTime accountExpiredDate;

	@Column(name = "account_non_locked", nullable = false)
	private boolean accountNonLocked;
	
	@Column(name = "account_lock_date")
	private LocalDateTime accountLockDate;

	@Column(name = "account_credentials_non_expired", nullable = false)
	private boolean credentialsNonExpired;
	
	@Column(name = "credential_expired_date")
	private LocalDateTime credentialExpiredDate;

	@Column(name = "is_active", nullable = false)
	private boolean isActive;

	@Column(name = "first_time_login_remaining", nullable = false)
	private boolean firstTimeloginRemaining;
	
	@Column(name = "first_time_login_date")
	private LocalDateTime firstTimeloginDate;
	
	@Column(name = "is_deleted", nullable = false)
	private boolean deleted;
	
	@Column(name = "last_time_password_updated")
	@CreationTimestamp
    @ApiModelProperty(hidden = true)
	private LocalDateTime lastTimePasswordUpdated;

	@Column(name = "password_never_expires", nullable = false)
	private boolean passwordNeverExpires;


    @ApiModelProperty(hidden = true)
    @JsonIgnore
    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinTable(
            name = "m_users_roles",
            joinColumns = @JoinColumn(
                    name = "user_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(
                    name = "role_id", referencedColumnName = "id"))
    private List<Roles> rolesList;

    @CreationTimestamp
    @ApiModelProperty(hidden = true)
    private LocalDateTime dateCreated;
    
    private LocalDateTime pinCreatedDate;
    
    private LocalDateTime dateOfActivation;

    public Users() {
    	provider = AuthProvider.local;
    	this.accountNonLocked = true;
		this.credentialsNonExpired = true;
		this.accountNonExpired = true;
    }

}
