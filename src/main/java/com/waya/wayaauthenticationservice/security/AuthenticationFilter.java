package com.waya.wayaauthenticationservice.security;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.waya.wayaauthenticationservice.exception.CustomException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.waya.wayaauthenticationservice.SpringApplicationContext;
import com.waya.wayaauthenticationservice.entity.Roles;
import com.waya.wayaauthenticationservice.entity.Users;
import com.waya.wayaauthenticationservice.pojo.others.LoginDetailsPojo;
import com.waya.wayaauthenticationservice.pojo.others.LoginResponsePojo;
import com.waya.wayaauthenticationservice.pojo.userDTO.UserProfileResponsePojo;
import com.waya.wayaauthenticationservice.repository.PrivilegeRepository;
import com.waya.wayaauthenticationservice.repository.UserRepository;
import com.waya.wayaauthenticationservice.service.LoginHistoryService;
import com.waya.wayaauthenticationservice.util.SecurityConstants;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AuthenticationFilter extends UsernamePasswordAuthenticationFilter {

	private final Gson gson = new Gson();
	private boolean isAdmin = false;

	@Autowired
	LoginHistoryService loginHistoryService;

	@Autowired
	PrivilegeRepository privilegeRepos;

	public AuthenticationFilter(AuthenticationManager manager) {
		super.setAuthenticationManager(manager);
	}

	public AuthenticationFilter() {
	}

	@Override
	public Authentication attemptAuthentication(HttpServletRequest req, HttpServletResponse res)
			throws AuthenticationException {
		try {
			LoginDetailsPojo creds = new ObjectMapper().readValue(req.getInputStream(), LoginDetailsPojo.class);
			isAdmin = creds.isAdmin();
			log.info("Admin is {}", isAdmin);

			UserRepository userLoginRepo = (UserRepository) SpringApplicationContext.getBean("userRepository");

			Users user = userLoginRepo.findByEmailOrPhoneNumber(creds.getEmailOrPhoneNumber())
					.orElseThrow(() -> new BadCredentialsException("User Does not exist"));

			List<Roles> roles = new ArrayList<Roles>(user.getRolesList());

			Collection<GrantedAuthority> grantedAuthorities = roles.stream().map(r -> {
				return new SimpleGrantedAuthority(r.getName());
			}).collect(Collectors.toSet());

			grantedAuthorities.addAll(getGrantedAuthorities(getPrivileges(roles)));

			// return getAuthenticationManager().authenticate(
			// new UsernamePasswordAuthenticationToken(creds.getEmail(),
			// creds.getPassword(), grantedAuthorities));

			return getAuthenticationManager().authenticate(
					new UsernamePasswordAuthenticationToken(creds.getEmailOrPhoneNumber(), creds.getPassword(), grantedAuthorities));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected void successfulAuthentication(HttpServletRequest req, HttpServletResponse res, FilterChain chain,
			Authentication auth) throws IOException, SignatureException {
		// Inspect Here
		Users user = ((UserPrincipal) auth.getPrincipal()).getUser().orElseThrow(() ->
				new CustomException("Error Fetching User Object", HttpStatus.UNPROCESSABLE_ENTITY));

		log.info("Signed in User ::: {}", user);
		String userName = user.getEmail();

		String token = Jwts.builder().setSubject(userName)
				.setExpiration(new Date(System.currentTimeMillis() + SecurityConstants.EXPIRATION_TIME))
				.signWith(SignatureAlgorithm.HS256, SecurityConstants.getSecret()).compact();

		LoginResponsePojo loginResponsePojo = new LoginResponsePojo();
		if(user.getAccountStatus() != -1){
			Map<String, Object> m = new HashMap<>();
			//if (user.isPhoneVerified() || user.isEmailVerified()) {
			if (!user.isActive()) {
				loginResponsePojo.setCode(-3);
				loginResponsePojo.setStatus(false);
				loginResponsePojo.setMessage("User account is disabled, kindly contact Waya Admin");
				res.setStatus(400);
			} else {
				Set<String> permit = getPrivileges(user.getRolesList());
				Set<String> roles = user.getRolesList().stream().map(u -> u.getName()).collect(Collectors.toSet());
				// true == true
				// if (isAdmin == roleCheck(rs, "ROLE_ADMIN")) {
				loginResponsePojo.setCode(0);
				loginResponsePojo.setStatus(true);
				loginResponsePojo.setMessage("Login Successful");

				m.put("token", SecurityConstants.TOKEN_PREFIX + token);
				m.put("privilege", permit);
				m.put("roles", roles);
				m.put("pinCreated", user.isPinCreated());
				m.put("corporate", user.isCorporate());

				res.addHeader(SecurityConstants.HEADER_STRING, SecurityConstants.TOKEN_PREFIX + token);

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

				m.put("user", userProfile);
				loginResponsePojo.setData(m);

				// } else {
				// loginResponsePojo.setCode(-3);
				// loginResponsePojo.setStatus(false);
				// loginResponsePojo.setMessage("Invalid Login");
				// }
			}
		} else {
			loginResponsePojo.setCode(-2);
			loginResponsePojo.setStatus(false);
			loginResponsePojo.setMessage("User's Password should be Changed ");
			res.setStatus(400);
		}

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
		Map<String, Comparable> m = new HashMap<String, Comparable>();
		m.put("code", -1);
		m.put("status", false);
		m.put("message", "Invalid Login");
		String str = gson.toJson(m);
		PrintWriter pr = response.getWriter();
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		pr.write(str);
	}

	public boolean roleCheck(Collection<Roles> rolesList, String role) {
		return rolesList.stream().anyMatch(e -> e.getName().equals(role));
	}

	private final Set<String> getPrivileges(final Collection<Roles> roles) {
		Set<String> privileges = new HashSet<String>();
		for (Roles role : roles) {
			privileges.addAll(role.getPermissions().stream().map(p -> p.getName()).collect(Collectors.toSet()));
		}
		return privileges;
	}

	private List<GrantedAuthority> getGrantedAuthorities(Set<String> privileges) {
		List<GrantedAuthority> authorities = new ArrayList<>();
		for (String privilege : privileges) {
			authorities.add(new SimpleGrantedAuthority(privilege));
		}
		return authorities;
	}
}
