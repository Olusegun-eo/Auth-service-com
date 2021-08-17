package com.waya.wayaauthenticationservice.integration;

import com.waya.wayaauthenticationservice.entity.Role;
import com.waya.wayaauthenticationservice.entity.Users;
import com.waya.wayaauthenticationservice.enums.ERole;
import com.waya.wayaauthenticationservice.pojo.userDTO.UserSetupPojo;
import com.waya.wayaauthenticationservice.repository.RolesRepository;
import com.waya.wayaauthenticationservice.repository.UserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.hamcrest.core.Is;
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

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;

import static com.waya.wayaauthenticationservice.util.JsonString.asJsonString;
import static com.waya.wayaauthenticationservice.util.SecurityConstants.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UserControllerTest {

	Users user = null;
	Role role = new Role();

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
		role = rolesRepository.findByName(ERole.ROLE_SUPER_ADMIN.name()).orElse(null);
		Optional<Users> userOpt = userRepository.findByEmailIgnoreCase("stan@toju.com");
		if(userOpt.isPresent()){
			user = userRepository.getOne(userOpt.get().getId());
		}

		if(user == null){
			user = new Users();
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
	}

	@Test
	@DisplayName("test User Controller Get User Setup: PASS")
	public void getUserSetUp() throws Exception {
		String token = generateToken(user);
		
		UserSetupPojo pojo = new UserSetupPojo();
		pojo.setUserId(user.getId());
		pojo.setTransactionLimit(new BigDecimal("300000"));

		saveUserSetUp(pojo, token,"$.message", "Successful",
				"$.status", true, status().isCreated());
		
		findUserSetup(user.getId(), token,"$.message", "Successful",
				"$.status", true, status().isOk());
	}

	@Test
	@DisplayName("test User Controller Save User Setup: PASS")
	public void saveUserSetup() throws Exception {
		String token = generateToken(user);
		UserSetupPojo pojo = new UserSetupPojo();
		pojo.setUserId(user.getId());
		pojo.setTransactionLimit(new BigDecimal("300000"));
		
		saveUserSetUp(pojo, token,"$.message", "Successful",
				"$.status", true, status().isCreated());
	}
	
	@Test
	@DisplayName("test User Controller Get User Setup: FAIL")
	public void getUserSetUpfAIL() throws Exception {
		String token = generateToken(user);
		long id = 4567;

		UserSetupPojo pojo = new UserSetupPojo();
		pojo.setUserId(id);
		pojo.setTransactionLimit(new BigDecimal("300000"));
		
		saveUserSetUp(pojo, token,"$.message", "Record with " +
				"Provided input not Found For User with " + id,
				"$.status", false, status().isNotFound());
		
		findUserSetup(id, token,"$.message", "No Setup Exists",
				"$.status", false, status().isNotFound());
	}

	@Test
	@DisplayName("test User Controller Save User Setup: FAIL")
	public void saveUserSetupFAIL() throws Exception {
		long id = 4567;
		String token = generateToken(user);
		UserSetupPojo pojo = new UserSetupPojo();
		pojo.setUserId(id);
		pojo.setTransactionLimit(new BigDecimal("300000"));

		saveUserSetUp(pojo, token,"$.message", "Record with " +
						"Provided input not Found For User with " + id,
				"$.status", false, status().isNotFound());
	}

	private void saveUserSetUp(UserSetupPojo pojo, String token,final String jsonPath0, final String jsonPathMessage0,
							   final String jsonPath1, final boolean jsonPathMessage1, ResultMatcher expectedStatus) throws Exception {
		
		mockMvc.perform(post("/api/v1/user/setup")
				.header("Authorization", token)
				.contentType(APPLICATION_JSON)
				.content(asJsonString(pojo)))
				.andExpect(expectedStatus)
				.andExpect(jsonPath(jsonPath0, Is.is(jsonPathMessage0)))
				.andExpect(jsonPath(jsonPath1, Is.is(jsonPathMessage1)))
				.andDo(print());
	}

	private void findUserSetup(long Id, String token, final String jsonPath0, final String jsonPathMessage0,
							   final String jsonPath1, final boolean jsonPathMessage1, ResultMatcher expectedStatus) throws Exception {
		mockMvc.perform(get("/api/v1/user/setup?id=" + Id)
				.header("Authorization", token)
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(expectedStatus)
				.andExpect(jsonPath(jsonPath0, Is.is(jsonPathMessage0)))
				.andExpect(jsonPath(jsonPath1, Is.is(jsonPathMessage1)))
				.andDo(print());
	}

	private String generateToken(Users user) {
		try {
			System.out.println("::::::GENERATE TOKEN:::::");
			String token = Jwts.builder().setSubject(user.getEmail())
					.setExpiration(new Date(System.currentTimeMillis() + getExpiration() * 1000))
					.signWith(SignatureAlgorithm.HS512, getSecret()).compact();
			System.out.println(":::::Token:::::");
			return TOKEN_PREFIX + token;
		} catch (Exception e) {
			throw new RuntimeException(e.fillInStackTrace());
		}
	}

}
