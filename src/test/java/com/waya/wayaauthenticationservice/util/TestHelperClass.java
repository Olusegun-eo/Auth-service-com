package com.waya.wayaauthenticationservice.util;

import com.waya.wayaauthenticationservice.entity.Roles;
import com.waya.wayaauthenticationservice.entity.Users;
import com.waya.wayaauthenticationservice.exception.CustomException;
import com.waya.wayaauthenticationservice.repository.RolesRepository;
import com.waya.wayaauthenticationservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

//@ActiveProfiles("test")
//@SpringBootTest(properties = {"eureka.client.enabled=false"})
//@AutoConfigureMockMvc
//@TestInstance(TestInstance.Lifecycle.PER_CLASS)
//@DirtiesContext
//@EmbeddedKafka(partitions = 1, brokerProperties =
//        {"listeners=PLAINTEXT://localhost:9092", "port=9092"})

public class TestHelperClass {

    private UserRepository userRepository;
    private RolesRepository rolesRepository;

    public TestHelperClass(UserRepository userRepository, RolesRepository rolesRepository) {
        this.userRepository = userRepository;
        this.rolesRepository = rolesRepository;
    }

    private final Users user = new Users();

    public void createTestUser(){



        user.setEmail("mike@app.com");
        user.setFirstName("Mike");
        user.setPhoneNumber("0029934");
        user.setReferenceCode("CRT");
        user.setSurname("Ang");
        user.setDateCreated(LocalDateTime.now());
        user.setAccountStatus(1);
        String fullName = String.format("%s %s", user.getFirstName(), user.getSurname());
        user.setName(fullName);

        Roles userRole = rolesRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new CustomException("User Role Not Available", HttpStatus.BAD_REQUEST));

    }

}
