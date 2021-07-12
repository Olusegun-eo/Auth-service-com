package com.waya.wayaauthenticationservice.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "m_corporate_user")
public class CorporateUser {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true)
	private String userId;

	@Column(nullable = false)
	private String businessType;

	@Column(nullable = false, unique = true)
	private String email;

    private String surname;
    private String firstName;
    private String city;
    private String officeAddress;

    @Column(nullable = false, unique = true)
    private String phoneNumber;

    private String password;
    private String state;
    private String orgName;
    private String orgEmail;
    private String orgPhone;
    private String orgType;
    private String referenceCode;
}
