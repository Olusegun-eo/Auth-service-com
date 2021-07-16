package com.waya.wayaauthenticationservice.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import com.waya.wayaauthenticationservice.util.CustomValidator;
import com.waya.wayaauthenticationservice.enums.Type;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "m_business_type")
public class BusinessType {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(unique = true)
	@NotNull(message = "Business Type cannot be Null")
	@CustomValidator(message = "Business Type cannot contain Numeric characters", type = Type.TEXT_STRING)
	private String businessType;
}
