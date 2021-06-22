package com.waya.wayaauthenticationservice.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;
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
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.validator.constraints.Length;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.waya.wayaauthenticationservice.model.AuthProvider;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@Entity
public class Users implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    private long id;

    @Email(message = "email should be valid")
    @Column(unique = true)
    private String email;
    
    @Column(name = "name", nullable = true)
    private String name;

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

    private boolean pinCreated = false;

    private boolean isCorporate = false;

    @Transient
    private Roles role;
    
    @NotNull
    @Enumerated(EnumType.STRING)
    private AuthProvider provider;

    private String providerId;
    
    private String imageUrl;
    
    @Column(name = "account_non_expired", nullable = false)
	private boolean accountNonExpired;

	@Column(name = "account_non_locked", nullable = false)
	private boolean accountNonLocked;

	@Column(name = "account_credentials_non_expired", nullable = false)
	private boolean credentialsNonExpired;

	@Column(name = "account_active", nullable = false)
	private boolean enabled;

	@Column(name = "first_time_login_remaining", nullable = false)
	private boolean firstTimeloginRemaining;

	@Column(name = "is_deleted", nullable = false)
	private boolean deleted;
	
	@Column(name = "last_time_password_updated")
	@Temporal(TemporalType.DATE)
	private Date lastTimePasswordUpdated;

	@Column(name = "password_never_expires", nullable = false)
	private boolean passwordNeverExpires;

    @ApiModelProperty(hidden = true)
    @JsonIgnore
    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinTable(
            name = "users_roles",
            joinColumns = @JoinColumn(
                    name = "user_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(
                    name = "role_id", referencedColumnName = "id"))
    private List<Roles> rolesList;

    @CreationTimestamp
    @ApiModelProperty(hidden = true)
    private LocalDateTime dateCreated;

    public Users() {
    	provider = AuthProvider.local;
    	this.accountNonLocked = false;
		this.credentialsNonExpired = false;
    }

}
