package com.waya.wayaauthenticationservice.entity;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.redis.core.RedisHash;

import javax.persistence.Id;
import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@RedisHash("User")
public class RedisUser implements Serializable {
    @Id
    private Long id;
    private String firstName;
    private String surname;
    private String email;
    private long phoneNumber;
    private List<Roles> roles;
}
