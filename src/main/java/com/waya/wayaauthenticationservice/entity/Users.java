package com.waya.wayaauthenticationservice.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.validator.constraints.Length;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
public class Users implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @NotNull
    private Long id;

    @Email(message = "email should be valid")
    @Column(unique = true)
    private String email;

    @NotNull(message = "phone number cannot be null")
    private Long phoneNumber;

    private String referenceCode;
    @NotBlank(message = "first Name cannot be null")

    private String firstName;

    @NotBlank(message = "last Name cannot be null")
    private String surname;

    @NotBlank(message = "password cannot be null")
    @Length(min = 8, max = 100, message = "password must be greater than 8 characters")
    private String password;

    private int pin;

    @Transient
    private Roles role;

    @ApiModelProperty(hidden = true)
    @JsonIgnore
    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinTable(
            name = "users_roles",
            joinColumns = @JoinColumn(
                    name = "user_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(
                    name = "role_id", referencedColumnName = "id"))
    private List<Roles> rolesList;

    @CreationTimestamp
    @ApiModelProperty(hidden = true)
    private LocalDateTime dateCreated;

    public Users() {}

}
