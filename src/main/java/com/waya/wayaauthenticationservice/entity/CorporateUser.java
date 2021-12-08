package com.waya.wayaauthenticationservice.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@Table(name = "m_corporate")
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class CorporateUser extends AuditModel implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
    private Long id;
	
	private String email;

    @Column(name = "phone_number")
    private String phoneNumber;
    
    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;
    
    private Long roleId;
    
    @Column(name = "invitee_id", nullable = false)
    private Long inviteeId;
    
    @Column(name = "invitor_id", nullable = false)
    private Long invitorId;
    
    @Column(nullable = false)
    @JsonIgnore
    private String passcode;
    
    @Column(nullable = false)
    private String name;
	

}
