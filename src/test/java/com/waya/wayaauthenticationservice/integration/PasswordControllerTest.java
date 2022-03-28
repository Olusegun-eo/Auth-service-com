package com.waya.wayaauthenticationservice.integration;

import static com.waya.wayaauthenticationservice.util.JsonString.asJsonString;
import static com.waya.wayaauthenticationservice.util.SecurityConstants.getExpiration;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.hamcrest.core.Is;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;

import com.waya.wayaauthenticationservice.entity.Users;
import com.waya.wayaauthenticationservice.exception.ErrorMessages;
import com.waya.wayaauthenticationservice.pojo.others.LoginDetailsPojo;
import com.waya.wayaauthenticationservice.pojo.password.PasswordPojo;
import com.waya.wayaauthenticationservice.pojo.password.ResetPasswordPojo;
import com.waya.wayaauthenticationservice.repository.UserRepository;
import com.waya.wayaauthenticationservice.response.OTPVerificationResponse;
import com.waya.wayaauthenticationservice.service.OTPTokenService;
import com.waya.wayaauthenticationservice.util.JwtUtil;
import com.waya.wayaauthenticationservice.util.SecurityConstants;

import lombok.extern.slf4j.Slf4j;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DirtiesContext
@Slf4j
public class PasswordControllerTest {
	
	JwtUtil jwtUtil = new JwtUtil();

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	BCryptPasswordEncoder passwordEncoder;

	private Users user = new Users();

	@MockBean
	private OTPTokenService OTPTokenService;

	@BeforeAll
	public void initialization() {
		user = setUpUser();
	}

	@Test
	@DisplayName("reset Password successfully with Email")
	public void resetPasswordSuccessfullyWithEmail() throws Exception {
		Mockito.when(OTPTokenService.verifyEmailToken(any(), any(), any()))
				.thenReturn(new OTPVerificationResponse(true, "Successful"));

		ResetPasswordPojo pojo = buildResetPojo(23456, "test@123", user.getEmail());
		resetPassword(pojo, "$.message", "Password Changed.", status().isOk());
	}

	@Test
	@DisplayName("reset Password successfully with PhoneNumber")
	public void resetPasswordSuccessfullyWithPhoneNumber() throws Exception {
		Mockito.when(OTPTokenService.verifySMSOTP(any(), any(), any()))
				.thenReturn(new OTPVerificationResponse(true, "Successful"));

		ResetPasswordPojo pojo = buildResetPojo(23456, "test@123", user.getPhoneNumber());
		resetPassword(pojo, "$.message", "Password Changed.", status().isOk());
	}

	@Test
	@DisplayName("reset Password Failure Test")
	public void resetPasswordFailed() throws Exception {
		Mockito.when(OTPTokenService.verifySMSOTP(any(), any(), any()))
				.thenReturn(new OTPVerificationResponse(true, "Successful"));

		ResetPasswordPojo pojo = buildResetPojo(23456, "test@123", "noemail@waya.com");
		resetPassword(pojo, "$.message",
				ErrorMessages.NO_RECORD_FOUND.getErrorMessage() + " For User with identity: noemail@waya.com",
				status().isBadRequest());
	}

	@Test
	@DisplayName("Change Forgotten Password Failure Test: Invalid Old Password")
	public void changeForgotPasswordFail() throws Exception {
		Mockito.when(OTPTokenService.verifySMSOTP(any(), any(), any()))
				.thenReturn(new OTPVerificationResponse(true, "Successful"));

		PasswordPojo pojo = buildChangePassPojo(234567, "test@12345", "test@12345", user.getPhoneNumber());
		changePassword(pojo, "$.message", "Incorrect Old Password", status().isBadRequest());

	}

	@Test
	@DisplayName("Change Forgotten Password Success Test: Correct Old Password")
	public void changeForgotPasswordPass() throws Exception {
		Mockito.when(OTPTokenService.verifySMSOTP(any(), any(), any()))
				.thenReturn(new OTPVerificationResponse(true, "Successful"));

		PasswordPojo pojo = buildChangePassPojo(234567, "test@123", "test@1234", user.getPhoneNumber());
		changePassword(pojo, "$.message", "Password Changed.", status().isOk());

		// Login with New PhoneNumber and new Password
		LoginDetailsPojo loginUser = new LoginDetailsPojo();
		loginUser.setEmailOrPhoneNumber(user.getPhoneNumber());
		loginUser.setPassword("test@1234");
		loginNewUser(loginUser, "$.message", "Login Successful", status().isOk());
	}

	private ResetPasswordPojo buildResetPojo(int otp, String newPassword, String phoneOrEmail) {
		ResetPasswordPojo pojo = new ResetPasswordPojo();
		pojo.setNewPassword(newPassword);
		pojo.setOtp(otp);
		pojo.setPhoneOrEmail(phoneOrEmail);
		return pojo;
	}

	private void changePassword(PasswordPojo pojo, String jsonPath0, String jsonPathMessage0,
			ResultMatcher expectedStatus) throws Exception {
		mockMvc.perform(post("/api/v1/password/change-password").header("Authorization", generateToken(user))
				.contentType(APPLICATION_JSON).content(asJsonString(pojo))).andExpect(expectedStatus)
				.andExpect(jsonPath(jsonPath0, Is.is(jsonPathMessage0))).andDo(print());
	}

	private void resetPassword(ResetPasswordPojo pojo, String jsonPath0, String jsonPathMessage0,
			ResultMatcher expectedStatus) throws Exception {
		mockMvc.perform(post("/api/v1/password/forgot-password")
				// .header("Authorization", generateToken(user))
				.contentType(APPLICATION_JSON).content(asJsonString(pojo))).andExpect(expectedStatus)
				.andExpect(jsonPath(jsonPath0, Is.is(jsonPathMessage0))).andDo(print());
	}

	private void loginNewUser(LoginDetailsPojo userPojo, String jsonPath, String jsonPathMessage,
			ResultMatcher expectedStatus) throws Exception {
		mockMvc.perform(post("/api/v1/auth/login").accept("application/json").contentType(APPLICATION_JSON)
				.content(asJsonString(userPojo))).andExpect(expectedStatus)
				.andExpect(jsonPath(jsonPath, Is.is(jsonPathMessage)));
	}

	private Users setUpUser() {
		user.setEmail("stan@tojue.com");
		user.setFirstName("Stan");
		user.setSurname("Toju");
		user.setActive(true);
		user.setPhoneNumber("2348160302443");
		user.setPassword(passwordEncoder.encode("test@123"));
		user.setName(String.format("%s %s", user.getFirstName(), user.getSurname()));
		return userRepository.save(user);
	}

	private PasswordPojo buildChangePassPojo(int otp, String oldPass, String newPassword, String phoneOrEmail) {
		PasswordPojo pojo = new PasswordPojo();
		pojo.setNewPassword(newPassword);
		pojo.setOldPassword(oldPass);
		pojo.setOtp(String.valueOf(otp));
		pojo.setPhoneOrEmail(phoneOrEmail);
		return pojo;
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
			
			return SecurityConstants.TOKEN_PREFIX + token;
		} catch (Exception e) {
			throw new RuntimeException(e.fillInStackTrace());
		}
	}

}
