package com.waya.wayaauthenticationservice.integration;

import static com.waya.wayaauthenticationservice.util.SecurityConstants.TOKEN_PREFIX;
import static com.waya.wayaauthenticationservice.util.SecurityConstants.getExpiration;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;

import com.waya.wayaauthenticationservice.entity.Role;
import com.waya.wayaauthenticationservice.entity.Users;
import com.waya.wayaauthenticationservice.enums.ERole;
import com.waya.wayaauthenticationservice.repository.RolesRepository;
import com.waya.wayaauthenticationservice.repository.UserRepository;
import com.waya.wayaauthenticationservice.util.JwtUtil;

import lombok.extern.slf4j.Slf4j;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DirtiesContext
@Slf4j
public class AdminControllerTest {

    Users user = new Users();
    JwtUtil jwtUtil = new JwtUtil();

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
        log.info(role.toString());
        user.setEmail("stan@toju.com");
        user.setFirstName("Stan");
        user.setSurname("Toju");
        user.setActive(true);
        user.setPhoneNumber("2348166302445");
        user.setPassword(passwordEncoder.encode("test@123"));
        user.setName(String.format("%s %s", user.getFirstName(), user.getSurname()));
        user.setRoleList(Collections.singleton(role));

        user = userRepository.save(user);
    }

    @Test
    @DisplayName("test Admin Controller Get All Users: PASS")
    public void getAllUsers() throws Exception {
        int page = 0;
        int size = 10;
        String token = generateToken(user);
        findALlUsersPageable(page, size, token, status().isOk() );
    }

    @Test
    @DisplayName("test Admin Controller Get All Users: FAIL")
    public void getAllUsersFail() throws Exception {
        int page = 0;
        int size = 10;
        String token = "token";
        findALlUsersPageable(page, size, token, status().is4xxClientError() );

    }

    private void findALlUsersPageable(int page, int size, String token, ResultMatcher expectedStatus) throws Exception {
        mockMvc.perform(get("/api/v1/admin/users?page="+page+"&size="+size)
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(expectedStatus)
                .andDo(print());
    }

    public String generateToken(Users user) {
        try {
            /*String token = Jwts.builder().setSubject(user.getEmail())
                    .setExpiration(new Date(System.currentTimeMillis() + getExpiration() * 1000))
                    .signWith(SignatureAlgorithm.HS512, getSecret()).compact();*/
        	Map<String, Object> claims = new HashMap<>();
	        claims.put("id", user.getId());
	        claims.put("role", user.getRoleList());
	        Date expirationDate = new Date(System.currentTimeMillis() + getExpiration());
			String token = jwtUtil.doGenerateToken(claims, user.getEmail(), expirationDate);
            System.out.println(":::::Token:::::" + TOKEN_PREFIX + token);
            return TOKEN_PREFIX + token;
        } catch (Exception e) {
            throw new RuntimeException(e.fillInStackTrace());
        }
    }

}
