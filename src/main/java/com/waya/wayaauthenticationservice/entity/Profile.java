package com.waya.wayaauthenticationservice.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import javax.validation.constraints.Email;
import java.util.UUID;

@Getter
@Setter
@Entity
@ToString
@Table(name = "m_user_profile")
public class Profile extends AuditModel {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false)
    @Email(message = "please enter a valid email")
    private String email;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String surname;

    @Column(nullable = false)
    private String phoneNumber;

    private String organisationName;

    private String middleName;

    private String profileImage;

    private String dateOfBirth;

    private String gender;

    private String age;

    private String district;

    private String address;

    private String city;

    private String state;

    private boolean deleted;

    @Column(nullable = false, unique = true)
    private String userId;

    private String referral;

    private boolean corporate;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "otherDetails_id", referencedColumnName = "id")
    private OtherDetails otherDetails;
}