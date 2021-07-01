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
<<<<<<< HEAD:src/main/java/com/waya/wayaauthenticationservice/entity/CooperateUser.java
@Table(name = "m_corporate_users")
public class CooperateUser {
=======
@Table(name = "m_corporate_user")
public class CoporateUser {
>>>>>>> 5b32112750c7ea61ccac03db912e4eef40653d63:src/main/java/com/waya/wayaauthenticationservice/entity/CoporateUser.java

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(nullable = false, unique = true)
	private Long userId;
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
