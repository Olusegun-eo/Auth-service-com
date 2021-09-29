package com.waya.wayaauthenticationservice.security;

import com.waya.wayaauthenticationservice.SpringApplicationContext;
import com.waya.wayaauthenticationservice.entity.Users;
import com.waya.wayaauthenticationservice.repository.UserRepository;
import com.waya.wayaauthenticationservice.util.SecurityConstants;
import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.util.StringUtils;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.waya.wayaauthenticationservice.util.HelperUtils.isEmail;
import static com.waya.wayaauthenticationservice.util.SecurityConstants.getSecret;

@Slf4j
public class AuthorizationFilter extends BasicAuthenticationFilter {

	public AuthorizationFilter(AuthenticationManager authManager) {
		super(authManager);
	}

	@Override
	protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
			throws IOException, ServletException {

		String header = req.getHeader(SecurityConstants.HEADER_STRING);

		if (header == null || !header.startsWith(SecurityConstants.TOKEN_PREFIX)) {
			chain.doFilter(req, res);
			return;
		}
		UsernamePasswordAuthenticationToken authentication = getAuthentication(req);
		SecurityContextHolder.getContext().setAuthentication(authentication);
		chain.doFilter(req, res);
	}

	private UsernamePasswordAuthenticationToken getAuthentication(HttpServletRequest request) {
		String token = parseJwt(request);
		String userToken;
		if (token != null && validateToken(token)) {
			userToken = getUserNameFromToken(token);
			if(userToken != null) {
				if(!isEmail(userToken)) {
					if(userToken.startsWith("+")) {
						userToken = userToken.substring(1);
					}
					if(userToken.length() > 10) {
						userToken = userToken.substring(userToken.length() - 10);
					}
				}
				UserRepository userLoginRepo = (UserRepository) SpringApplicationContext.getBean("userRepository");

				Users user = userLoginRepo.findByEmailOrPhoneNumber(userToken).orElse(null);
				if (user != null) {
					UserPrincipal userPrincipal = new UserPrincipal(user);
					UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
							userPrincipal, null, userPrincipal.getAuthorities());

					authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
					SecurityContextHolder.getContext().setAuthentication(authentication);

					log.info(userPrincipal.toString());
					return authentication;
				}
				return null;
			}
		}
		return new UsernamePasswordAuthenticationToken(null, null, null);
	}

	private boolean validateToken(String authToken) {
		try {
			Jwts.parser().setSigningKey(getSecret()).parseClaimsJws(authToken);
			return true;
		} catch (SignatureException ex) {
			log.error("Invalid JWT signature");
		} catch (MalformedJwtException ex) {
			log.error("Invalid JWT token");
		} catch (ExpiredJwtException ex) {
			log.error("Expired JWT token");
		} catch (UnsupportedJwtException ex) {
			log.error("Unsupported JWT token");
		} catch (IllegalArgumentException ex) {
			log.error("JWT claims string is empty.");
		}
		return false;
	}

	private String parseJwt(HttpServletRequest request) {
		String headerAuth = request.getHeader(SecurityConstants.HEADER_STRING);

		if (StringUtils.hasText(headerAuth) && headerAuth.startsWith(SecurityConstants.TOKEN_PREFIX)) {
			return headerAuth.replace(SecurityConstants.TOKEN_PREFIX, "");
		}
		return null;
	}

	private String getUserNameFromToken(String token) {
		return Jwts.parser().setSigningKey(getSecret()).parseClaimsJws(token).getBody().getSubject();
	}
}
