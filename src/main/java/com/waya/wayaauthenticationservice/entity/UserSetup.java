package com.waya.wayaauthenticationservice.entity;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@Entity
@ToString
@Table(name = "m_users_setup")
@NoArgsConstructor
@AllArgsConstructor
public class UserSetup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "users_limit")
    private BigDecimal transactionLimit;

    @Column(name = "is_deleted")
    private boolean isDeleted = false;

	@OneToOne
	@JoinColumn(name = "user_id")
    private Users user;
}
