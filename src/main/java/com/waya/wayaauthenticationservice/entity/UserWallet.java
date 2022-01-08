package com.waya.wayaauthenticationservice.entity;

import java.util.Date;

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
import lombok.ToString;

@Entity
@Getter
@Setter
@Table(name = "m_user_wallet")
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class UserWallet extends AuditModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7386692254393983440L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	private Long id;

	@Column(name = "is_deleted", nullable = false)
	private boolean isDeleted = false;

	@Column(unique = true, nullable = false)
	private Long userId;

	private String fullName;

	private String phoneNo;

	private String email;

	private String city;

	private String district;

	private boolean isCardLinked;

	private String status;

	private String usertype;

	private Date createdDate;

	private String wallet;

	private boolean isWebPos;

	private boolean isTerminalPos;

	public UserWallet(boolean isDeleted, Long userId, String fullName, String phoneNo, String email, String city,
			String district, boolean isCardLinked, String status, String usertype, Date createdDate,
			String wallet) {
		super();
		this.isDeleted = isDeleted;
		this.userId = userId;
		this.fullName = fullName;
		this.phoneNo = phoneNo;
		this.email = email;
		this.city = city;
		this.district = district;
		this.isCardLinked = isCardLinked;
		this.status = status;
		this.usertype = usertype;
		this.createdDate = createdDate;
		this.wallet = wallet;
		this.isWebPos = false;
		this.isTerminalPos = false;
	}
	

}
