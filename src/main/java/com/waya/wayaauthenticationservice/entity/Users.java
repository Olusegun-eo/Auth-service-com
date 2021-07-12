package com.waya.wayaauthenticationservice.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.waya.wayaauthenticationservice.model.AuthProvider;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Objects;

@Data
@Entity
@Table(name = "m_users", uniqueConstraints = {
        @UniqueConstraint(name = "UniqueEmailAndPhoneNumberAndDelFlg", columnNames = {"phone_number", "email", "is_deleted"})})
public class Users implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    private Long id;

    //@Column(name = "user_id", unique = true, nullable = false)
    //private String userId;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, name = "phone_number", unique = true)
    private String phoneNumber;

    private String referenceCode;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String surname;

    @JsonIgnore
    @Column(nullable = false)
    private String password;

    @JsonIgnore
    private String pinHash;

    @JsonIgnore
    @Column(nullable = false)
    private String name;

    @JsonIgnore
    private boolean phoneVerified = false;

    @JsonIgnore
    private boolean emailVerified = false;

    @Column(name = "email_verified_date")
    private LocalDateTime emailVerifiedDate;

    private boolean pinCreated = false;

    private boolean isCorporate = false;

    private boolean isAdmin = false;

    @NotNull
    @Enumerated(EnumType.STRING)
    private AuthProvider provider;

    private String providerId;

    private String regDeviceType;

    private String regDevicePlatform;

    private String regDeviceIP;

    private String imageUrl;

    @Column(name = "account_status")
    private int accountStatus = 1;

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
    private boolean isActive = false;

    @Column(name = "first_time_login_remaining", nullable = false)
    private boolean firstTimeLoginRemaining;

    @Column(name = "first_time_login_date")
    private LocalDateTime firstTimeLoginDate;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    @Column(name = "last_time_password_updated")
    @CreationTimestamp
    @ApiModelProperty(hidden = true)
    private LocalDateTime lastTimePasswordUpdated;

    @Column(name = "password_never_expires", nullable = false)
    private boolean passwordNeverExpires;

    @ApiModelProperty(hidden = true)
    @JsonIgnore
    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinTable(name = "m_users_roles", joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"), inverseJoinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id"))
    private Collection<Roles> rolesList;

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

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Users)) {
            return false;
        }
        Users other = (Users) obj;
        return Objects.equals(email, other.email) && id == other.id && Objects.equals(phoneNumber, other.phoneNumber)
                && Objects.equals(surname, other.surname);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email, id, phoneNumber, surname);
    }

}