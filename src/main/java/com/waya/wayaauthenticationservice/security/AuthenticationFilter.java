package com.waya.wayaauthenticationservice.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.waya.wayaauthenticationservice.SpringApplicationContext;
import com.waya.wayaauthenticationservice.entity.*;
import com.waya.wayaauthenticationservice.pojo.access.UserAccessResponse;
import com.waya.wayaauthenticationservice.pojo.others.LoginDetailsPojo;
import com.waya.wayaauthenticationservice.pojo.others.LoginResponsePojo;
import com.waya.wayaauthenticationservice.pojo.userDTO.UserProfileResponsePojo;
import com.waya.wayaauthenticationservice.repository.ReferralCodeRepository;
import com.waya.wayaauthenticationservice.repository.UserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import lombok.NoArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.waya.wayaauthenticationservice.util.HelperUtils.emailPattern;
import static com.waya.wayaauthenticationservice.util.SecurityConstants.*;

@NoArgsConstructor
public class AuthenticationFilter extends UsernamePasswordAuthenticationFilter {

	private final Gson gson = new Gson();
	private String loginPrincipal = "";
	// private boolean isAdmin = false;

	public AuthenticationFilter(AuthenticationManager manager) {
		super.setAuthenticationManager(manager);
	}

	@Override
	public Authentication attemptAuthentication(HttpServletRequest req, HttpServletResponse res)
			throws AuthenticationException {
		try {
			LoginDetailsPojo creds = new ObjectMapper().readValue(req.getInputStream(), LoginDetailsPojo.class);
			String principal = creds.getEmailOrPhoneNumber().replaceAll("\\s+", "").trim();
			boolean isEmail = emailPattern.matcher(principal).matches();
			if(!isEmail) {
				if(principal.startsWith("+")) {
					principal = principal.substring(1);
				}
				if(principal.length() > 10) {
					principal = principal.substring(principal.length() - 10);
				}
			}
			loginPrincipal = principal;
			return getAuthenticationManager().authenticate(new UsernamePasswordAuthenticationToken(
					principal, creds.getPassword(), new ArrayList<>()));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected void successfulAuthentication(HttpServletRequest req, HttpServletResponse res, FilterChain chain,
			Authentication auth) throws IOException, SignatureException {
		// Inspect Here
		if (auth != null && auth.getPrincipal() instanceof UserPrincipal) {
			UserPrincipal userPrincipal = ((UserPrincipal) auth.getPrincipal());
			Users user = userPrincipal.getUser().orElse(null);
			if (user == null)
				return;
			UserAccessResponse access = userPrincipal.getAccess();

			String userName = (user.getEmail() == null || user.getEmail().isBlank()) ? user.getPhoneNumber() :  user.getEmail();

			String token = Jwts.builder().setSubject(userName)
					.setExpiration(new Date(System.currentTimeMillis() + getExpiration()))
					.signWith(SignatureAlgorithm.HS256, getSecret()).compact();

			// Check for First Login Attempt and Update User Table
			UserRepository userRepository = (UserRepository) SpringApplicationContext.getBean("userRepository");
			if (user.isFirstTimeLogin()) {
				user.setFirstTimeLogin(false);
				user.setFirstTimeLoginDate(LocalDateTime.now());
				if (userRepository != null){
					userRepository.save(user);
				}
			}
			ReferralCodeRepository referralRepo = SpringApplicationContext.getBean(ReferralCodeRepository.class);
			ReferralCode referral = Objects.requireNonNull(referralRepo).getReferralCodeByUserId(String.valueOf(user.getId()))
					.orElse(new ReferralCode());

			LoginResponsePojo loginResponsePojo = new LoginResponsePojo();
			Map<String, Object> m = new HashMap<>();

			Set<String> permit = getPrivileges(user.getRoleList());
			Set<String> roles = user.getRoleList().stream().map(Role::getName).collect(Collectors.toSet());

			loginResponsePojo.setCode(0);
			loginResponsePojo.setStatus(true);
			loginResponsePojo.setMessage("Login Successful");

			m.put("token", TOKEN_PREFIX + token);
			m.put("privilege", permit);
			m.put("roles", roles);
			m.put("access", access);
			m.put("pinCreated", user.isPinCreated());
			m.put("corporate", user.isCorporate());

			res.addHeader(HEADER_STRING, TOKEN_PREFIX + token);
			UserProfileResponsePojo userProfile = convert(user, referral);

			m.put("user", userProfile);
			loginResponsePojo.setData(m);
			String str = gson.toJson(loginResponsePojo);
			PrintWriter pr = res.getWriter();
			res.setContentType("application/json");
			res.setCharacterEncoding("UTF-8");

			pr.write(str);
		}
	}

	@Override
	protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException failed) throws IOException {

		UserRepository userRepository = (UserRepository) SpringApplicationContext.getBean("userRepository");
		Users user = userRepository.findByEmailOrPhoneNumber(this.loginPrincipal).orElse(null);

		String errorMessage = "";
		if(user != null){
			if(!user.isAccountNonExpired()) errorMessage = "Account is Expired";
			else if(!user.isAccountNonLocked())  errorMessage = "Account is Locked, Contact WAYA Support";
			else if(!user.isActive()) errorMessage = "Account not verifield";
			else errorMessage = failed != null ? failed.getMessage() : "Invalid Login";
		}


		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		response.setContentType(MediaType.ALL_VALUE);
		@SuppressWarnings("rawtypes")
		Map<String, Comparable> m = new HashMap<>();
		m.put("code", -1);
		m.put("status", false);
		m.put("message", errorMessage);
		String str = gson.toJson(m);
		PrintWriter pr = response.getWriter();
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		pr.write(str);
	}

	private Set<String> getPrivileges(final Collection<Role> roles) {
		Set<String> privileges = new HashSet<>();
		for (Role role : roles) {
			privileges.addAll(role.getPrivileges().stream().map(Privilege::getName).collect(Collectors.toSet()));
		}
		return privileges;
	}

	private UserProfileResponsePojo convert(Users user, ReferralCode referral) {
		Profile profile = referral.getProfile();
		String referralCode = referral.getReferralCode();

		Set<String> permit = getPrivileges(user.getRoleList());
		Set<String> roles = user.getRoleList().stream().map(Role::getName).collect(Collectors.toSet());

		UserProfileResponsePojo userProfile = new UserProfileResponsePojo();

		userProfile.setId(user.getId());
		userProfile.setEmail(Objects.toString(user.getEmail(), ""));
		userProfile.setPhoneNumber(Objects.toString(user.getPhoneNumber(), ""));
		userProfile.setReferenceCode(Objects.toString(referralCode, ""));
		userProfile.setFirstName(user.getFirstName());
		userProfile.setLastName(user.getSurname());
		userProfile.setAdmin(user.isAdmin());
		userProfile.setPinCreated(user.isPinCreated());
		userProfile.setCorporate(user.isCorporate());
		userProfile.setEmailVerified(user.isEmailVerified());
		userProfile.setPhoneVerified(user.isPhoneVerified());
		userProfile.setActive(user.isActive());
		userProfile.setAccountDeleted(user.isDeleted());
		userProfile.setRoles(roles);
		userProfile.setPermits(permit);
		userProfile.setAccountExpired(!user.isAccountNonExpired());
		userProfile.setAccountLocked(!user.isAccountNonLocked());
		userProfile.setCredentialsExpired(!user.isCredentialsNonExpired());

		userProfile.setGender(Objects.toString(profile.getGender(), ""));
		userProfile.setMiddleName(Objects.toString(profile.getMiddleName(), ""));
		userProfile.setDateOfBirth(Objects.toString(profile.getDateOfBirth(), ""));
		userProfile.setDistrict(Objects.toString(profile.getDistrict(), ""));
		userProfile.setAddress(Objects.toString(profile.getAddress(), ""));
		userProfile.setCity(Objects.toString(profile.getCity(), ""));
		userProfile.setState(Objects.toString(profile.getState(), ""));
		userProfile.setProfileImage(Objects.toString(profile.getProfileImage(), ""));

		return userProfile;
	}

}
