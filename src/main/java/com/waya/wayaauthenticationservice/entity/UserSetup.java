package com.waya.wayaauthenticationservice.entity;

import lombok.*;

import javax.persistence.*;
import java.math.BigDecimal;

@Setter
@Getter
@Entity
@ToString
@Table(name = "m_users_setup")
@NoArgsConstructor
@AllArgsConstructor
public class UserSetup extends AuditModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "users_limit")
    private BigDecimal transactionLimit;

    @Column(name = "is_deleted")
    private boolean isDeleted = false;
    
    @Column(name = "is_updated")
    private boolean isUpdated = false;

	@OneToOne
	@JoinColumn(name = "user_id")
    private Users user;
}
