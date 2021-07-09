package com.waya.wayaauthenticationservice.pojo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotBlank;
import java.util.UUID;

@Data
public class OtherDetailsDto {
    private UUID id;
    private String organisationName;
    private String organisationType;
    private String businessType;
    private ProfileDto profile;
}
