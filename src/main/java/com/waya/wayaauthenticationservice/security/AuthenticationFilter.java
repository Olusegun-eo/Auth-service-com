package com.waya.wayaauthenticationservice.security;

import static com.waya.wayaauthenticationservice.util.HelperUtils.emailPattern;
import static com.waya.wayaauthenticationservice.util.SecurityConstants.HEADER_STRING;
import static com.waya.wayaauthenticationservice.util.SecurityConstants.TOKEN_PREFIX;
import static com.waya.wayaauthenticationservice.util.SecurityConstants.getExpiration;
import static com.waya.wayaauthenticationservice.util.SecurityConstants.getSecret;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.modelmapper.ModelMapper;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.waya.wayaauthenticationservice.SpringApplicationContext;
import com.waya.wayaauthenticationservice.entity.Profile;
import com.waya.wayaauthenticationservice.entity.Role;
import com.waya.wayaauthenticationservice.entity.Users;
import com.waya.wayaauthenticationservice.pojo.access.UserAccessResponse;
import com.waya.wayaauthenticationservice.pojo.others.LoginDetailsPojo;
import com.waya.wayaauthenticationservice.pojo.others.LoginResponsePojo;
import com.waya.wayaauthenticationservice.pojo.userDTO.UserProfileResponsePojo;
import com.waya.wayaauthenticationservice.repository.ProfileRepository;
import com.waya.wayaauthenticationservice.repository.UserRepository;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class AuthenticationFilter extends UsernamePasswordAuthenticationFilter {

	private final Gson gson = new Gson();
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
		UserPrincipal userPrincipal = ((UserPrincipal) auth.getPrincipal());
		Users user = userPrincipal.getUser().orElse(null);
		if (user == null)
			return;
		UserAccessResponse access = userPrincipal.getAccess();

		String userName = user.getEmail();
		String token = Jwts.builder().setSubject(userName)
				.setExpiration(new Date(System.currentTimeMillis() + getExpiration()))
				.signWith(SignatureAlgorithm.HS256, getSecret()).compact();
		// Check for First Login Attempt and Update User Table
		UserRepository userRepository = (UserRepository) SpringApplicationContext.getBean("userRepository");
		if (user.isFirstTimeLogin()) {
			user.setFirstTimeLogin(false);
			user.setFirstTimeLoginDate(LocalDateTime.now());
			userRepository.save(user);
		}
		ProfileRepository profileRepository = (ProfileRepository) SpringApplicationContext.getBean("profileRepository");
		Profile profile = profileRepository.findByUserId(false, String.valueOf(user.getId())).orElse(new Profile());

		LoginResponsePojo loginResponsePojo = new LoginResponsePojo();
		Map<String, Object> m = new HashMap<>();

		Set<String> permit = getPrivileges(user.getRoleList());
		Set<String> roles = user.getRoleList().stream().map(u -> u.getName()).collect(Collectors.toSet());
		
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
		UserProfileResponsePojo userProfile = convert(user, profile);

		m.put("user", userProfile);
		loginResponsePojo.setData(m);
		String str = gson.toJson(loginResponsePojo);
		PrintWriter pr = res.getWriter();
		res.setContentType("application/json");
		res.setCharacterEncoding("UTF-8");

		pr.write(str);
	}

	@Override
	protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException failed) throws IOException {

		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		response.setContentType(MediaType.ALL_VALUE);
		@SuppressWarnings("rawtypes")
		Map<String, Comparable> m = new HashMap<>();
		m.put("code", -1);
		m.put("status", false);
		String errorMessage = failed != null ? failed.getMessage() : "Invalid Login";
		m.put("message", errorMessage);
		String str = gson.toJson(m);
		PrintWriter pr = response.getWriter();
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		pr.write(str);
	}

	public boolean roleCheck(Collection<Role> roleList, String role) {
		return roleList.stream().anyMatch(e -> e.getName().equals(role));
	}

	private Set<String> getPrivileges(final Collection<Role> roles) {
		Set<String> privileges = new HashSet<>();
		for (Role role : roles) {
			privileges.addAll(role.getPrivileges().stream().map(p -> p.getName()).collect(Collectors.toSet()));
		}
		return privileges;
	}

	private UserProfileResponsePojo convert(Users user, Profile profile) {
		Set<String> permit = getPrivileges(user.getRoleList());
		Set<String> roles = user.getRoleList().stream().map(u -> u.getName()).collect(Collectors.toSet());

		UserProfileResponsePojo userProfile = new ModelMapper().map(user, UserProfileResponsePojo.class);
		userProfile.setPhoneNumber(user.getPhoneNumber());
		userProfile.setFirstName(user.getFirstName());
		userProfile.setLastName(user.getSurname());
		userProfile.setEmailVerified(user.isEmailVerified());
		userProfile.setActive(user.isActive());
		userProfile.setAccountDeleted(user.isDeleted());
		userProfile.setAdmin(user.isAdmin());
		userProfile.setRoles(roles);
		userProfile.setPermits(permit);
		userProfile.setAccountExpired(!user.isAccountNonExpired());
		userProfile.setAccountLocked(!user.isAccountNonLocked());
		userProfile.setCredentialsExpired(!user.isCredentialsNonExpired());
		// if(profile != null){
		userProfile.setGender(profile.getGender());
		userProfile.setMiddleName(profile.getMiddleName());
		userProfile.setDateOfBirth(profile.getDateOfBirth());
		userProfile.setDistrict(profile.getDistrict());
		userProfile.setAddress(profile.getAddress());
		userProfile.setCity(profile.getCity());
		userProfile.setState(profile.getState());
		userProfile.setProfileImage(profile.getProfileImage());
		// }
		return userProfile;
	}

}
