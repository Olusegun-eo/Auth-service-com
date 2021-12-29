package com.waya.wayaauthenticationservice.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString
@Table(name = "m_password_policy")
public class PasswordPolicy {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    private Long id;
	
	@Column(nullable = false)
	private boolean del_flg;
	
	@OneToOne
    private Users user;
	
	@Column(nullable = false)
    private LocalDateTime rcreDate;
    
    private LocalDateTime lchgDate;
    
    @Column(nullable = false)
    private String newPassword;
    
    private String oldPassword;
    
    private String secondOldPassword;
    
    private String thirdOldPassword;
    
    private String fouthOldPassword;
    
    private int passwordCnt;
    
    private LocalDate updatedPasswordDate;
    
    private LocalDateTime changePasswordDate;
    
    private int passwordAge;
    
    private String token;
    
    @Column(columnDefinition="TEXT")
    private int tokenAge;
    
    private LocalDate updatedTokenDate;
    
    private LocalDateTime changeTokenDate;
    
    private LocalDate runDate;

	public PasswordPolicy() {
		super();
	}

	public PasswordPolicy(Users user, String newPassword) {
		super();
		this.del_flg = false;
		this.user = user;
		this.rcreDate = LocalDateTime.now();
		this.lchgDate = LocalDateTime.now();
		this.newPassword = newPassword;
		this.oldPassword = null;
		this.secondOldPassword = null;
		this.thirdOldPassword = null;
		this.fouthOldPassword = null;
		this.passwordCnt = 0;
		this.updatedPasswordDate = LocalDate.now();
		this.changePasswordDate = LocalDateTime.now();
		this.passwordAge = 0;
		this.token = null;
		this.tokenAge = 0;
		this.updatedTokenDate = LocalDate.now();
		this.changeTokenDate = LocalDateTime.now();
		this.runDate = LocalDate.now();
	}
    
    
    
}
