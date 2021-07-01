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
    /**
	 * 
	 */
<<<<<<< HEAD
	private static final long serialVersionUID = -7426379250883171400L;
=======
	private static final long serialVersionUID = 1L;
>>>>>>> 5b32112750c7ea61ccac03db912e4eef40653d63
	@Id
    private Long id;
    private String firstName;
    private String surname;
    private String email;
    private String phoneNumber;
    private List<Roles> roles;
}
