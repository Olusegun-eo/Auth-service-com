package com.waya.wayaauthenticationservice.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Data
@Entity
@Table(name = "m_login_history")
@NoArgsConstructor
@AllArgsConstructor
public class LoginHistory implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(unique = true, nullable = false)
    private long id;
    private String ip;
    private String device;
    private String city;
    private String province;
    private String country;

    @ManyToOne
    private Users user;

    @CreationTimestamp
    private Date loginDate;


}