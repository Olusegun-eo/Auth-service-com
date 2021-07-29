package com.waya.wayaauthenticationservice.integration;

import com.waya.wayaauthenticationservice.entity.Role;
import com.waya.wayaauthenticationservice.entity.Users;
import com.waya.wayaauthenticationservice.enums.ERole;
import com.waya.wayaauthenticationservice.repository.RolesRepository;
import com.waya.wayaauthenticationservice.repository.UserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;

import java.util.Collections;
import java.util.Date;

import static com.waya.wayaauthenticationservice.util.Constant.*;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AdminControllerTest {

    Users user = new Users();

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RolesRepository rolesRepository;

    @Autowired
    BCryptPasswordEncoder passwordEncoder;

    @BeforeAll
    public void setUp() {

        Role role = rolesRepository.findByName(ERole.ROLE_SUPER_ADMIN.name()).orElse(null);

        user.setEmail("stan@toju.com");
        user.setFirstName("Stan");
        user.setSurname("Toju");
        user.setActive(true);
        user.setPhoneNumber("2348166302445");
        user.setPassword(passwordEncoder.encode("test@123"));
        user.setName(String.format("%s %s", user.getFirstName(), user.getSurname()));
        user.setId(1l);
        user.setRoleList(Collections.singleton(role));

        userRepository.save(user);
    }

    @Test
    @DisplayName("test Admin Controller Get All Users: PASS")
    public void getAllUsers() throws Exception {
        int page = 0;
        int size = 10;
        String token = generateToken(user);
        findALlUsersPageable(page, size, "$._embedded.userResponse[*].email", user.getEmail(), token, status().isOk() );
    }

    @Test
    @DisplayName("test Admin Controller Get All Users: FAIL")
    public void getAllUsersFail() throws Exception {
        int page = 0;
        int size = 10;
        String token = "token";

    }

    private void findALlUsersPageable(int page, int size, String jsonPath0, String jsonPathMessage, String token, ResultMatcher expectedStatus) throws Exception {
        mockMvc.perform(get("/api/v1/admin/users?page="+page+"&size="+size)
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(expectedStatus)
                //.andExpect(jsonPath(jsonPath0, Is.is(jsonPathMessage)))
                .andExpect(jsonPath(jsonPath0).value(containsInAnyOrder(jsonPathMessage)))
                .andDo(print());
    }

    public String generateToken(Users user) {
        try {
            System.out.println("::::::GENERATE TOKEN:::::");
            String token = Jwts.builder().setSubject(user.getEmail())
                    .setExpiration(new Date(System.currentTimeMillis() + JWT_TOKEN_VALIDITY * 1000))
                    .signWith(SignatureAlgorithm.HS512, SECRET_TOKEN).compact();
            System.out.println(":::::Token:::::");
            return TOKEN_PREFIX + token;
        } catch (Exception e) {
            throw new RuntimeException(e.fillInStackTrace());
        }
    }

}
