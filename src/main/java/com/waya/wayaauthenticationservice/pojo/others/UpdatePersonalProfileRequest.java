package com.waya.wayaauthenticationservice.pojo.others;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.waya.wayaauthenticationservice.enums.Type;
import com.waya.wayaauthenticationservice.util.CustomValidator;
import com.waya.wayaauthenticationservice.util.ValidPhone;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
public class UpdatePersonalProfileRequest {

    @JsonIgnore
    private UUID id;

    @NotBlank(message = "please provide your email")
    @Email(message = "please enter a valid email")
    private String email;

    @CustomValidator(message = "firstName must not contain numeric", type = Type.TEXT_STRING)
    @NotBlank(message = "please provide your first name")
    private String firstName;

    @NotBlank(message = "please provide your surname")
    private String surname;

    @NotBlank(message = "please provide your phone number")
    @ValidPhone
    private String phoneNumber;

    @CustomValidator(message = "middleName must not contain numeric", type = Type.TEXT_STRING)
    private String middleName;

    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate dateOfBirth = LocalDate.now();

    private String gender;

    private String district;

    private String address;
}

