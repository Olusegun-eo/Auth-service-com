package com.waya.wayaauthenticationservice.util;

import com.waya.wayaauthenticationservice.entity.Role;
import com.waya.wayaauthenticationservice.entity.Users;
import com.waya.wayaauthenticationservice.exception.CustomException;
import com.waya.wayaauthenticationservice.repository.RolesRepository;
import com.waya.wayaauthenticationservice.repository.UserRepository;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.Collections;

//@ActiveProfiles("test")
//@SpringBootTest(properties = {"eureka.client.enabled=false"})
//@AutoConfigureMockMvc
//@TestInstance(TestInstance.Lifecycle.PER_CLASS)
//@DirtiesContext
//@EmbeddedKafka(partitions = 1, brokerProperties =
//        {"listeners=PLAINTEXT://localhost:9092", "port=9092"})

public class TestHelper {

    private UserRepository userRepository;
    private RolesRepository rolesRepository;

    public TestHelper(UserRepository userRepository, RolesRepository rolesRepository) {
        this.userRepository = userRepository;
        this.rolesRepository = rolesRepository;
    }

    private final Users user = new Users();

    public Users createTestUser(){
        Role userRole = this.rolesRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new CustomException("User Role Not Available", HttpStatus.BAD_REQUEST));

        user.setEmail("mike@app.com");
        user.setFirstName("Mike");
        user.setPassword("test@123");
        user.setPhoneNumber("0029934");
        user.setReferenceCode("CRT");
        user.setSurname("Ang");
        user.setDateCreated(LocalDateTime.now());
        user.setAccountStatus(1);
        String fullName = String.format("%s %s", user.getFirstName(), user.getSurname());
        user.setName(fullName);
        user.setRoleList(Collections.singletonList(userRole));
        Users regUser;
        if(userRepository.existsByEmail(user.getEmail()) || userRepository.existsByPhoneNumber(user.getEmail()))
            regUser = user;
        else
            regUser = userRepository.save(user);

        return regUser;
    }

}
