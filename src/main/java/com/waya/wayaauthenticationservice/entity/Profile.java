package com.waya.wayaauthenticationservice.entity;

import com.waya.wayaauthenticationservice.entity.listener.ProfileListener;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.Email;
import java.io.Serializable;
import java.util.UUID;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@EntityListeners(ProfileListener.class)
@Table(name = "m_user_profile")
public class Profile extends AuditModel implements Serializable {

	private static final long serialVersionUID = 1122422343339889166L;

	@Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Email(message = "please enter a valid email")
    private String email;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String surname;

    private String phoneNumber;

    private String middleName;

    private String profileImage;

    private String dateOfBirth;

    private String gender;
    //private String age;
    private String district;

    private String address;

    private String city;

    private String state;

    private boolean deleted = false;

    @Column(nullable = false, unique = true)
    private String userId;

    private String referral;

    private boolean corporate;

    private String deviceToken;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "otherDetails_id", referencedColumnName = "id")
    private OtherDetails otherDetails;


    @Override
    public String toString() {
        return "Profile{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", firstName='" + firstName + '\'' +
                ", surname='" + surname + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", middleName='" + middleName + '\'' +
                ", profileImage='" + profileImage + '\'' +
                ", dateOfBirth='" + dateOfBirth + '\'' +
                ", gender='" + gender + '\'' +
                ", district='" + district + '\'' +
                ", address='" + address + '\'' +
                ", city='" + city + '\'' +
                ", state='" + state + '\'' +
                ", deleted=" + deleted +
                ", userId='" + userId + '\'' +
                ", referral='" + referral + '\'' +
                ", corporate=" + corporate +
                ", otherDetails=" + otherDetails +
                '}';
    }
}