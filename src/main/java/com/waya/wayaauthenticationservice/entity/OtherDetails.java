package com.waya.wayaauthenticationservice.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "m_profile_other_details")
public class OtherDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @NotBlank(message = "please enter your organisation name")
    private String organisationName;

    @NotBlank(message = "please enter your organisation type")
    private String organisationType;

    @NotBlank(message = "please enter your business type")
    private String businessType;

    private String frontImage;

    private String leftImage;

    private String rightImage;

    @JsonIgnore
    @OneToOne(mappedBy = "otherDetails")
    private Profile profile;
}