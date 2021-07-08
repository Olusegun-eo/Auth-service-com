package com.waya.wayaauthenticationservice.security;

import java.io.IOException;
import java.util.Collections;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
@Service
public class TokenAuthenticationService {
	
	private static final Logger logger = LoggerFactory.getLogger(TokenAuthenticationService.class);
	// EXPIRATION_TIME = 10 dias 1 minute = 60 seconds = 60 × 1000 milliseconds = 60,000 ms
	static final long EXPIRATION_TIME = 432_000_000; //432_000_000 //300_000 // 860_000_000
	static final String SECRET = "MySecret";
	static final String TOKEN_PREFIX = "Bearer";
	static final String HEADER_STRING = "Authorization";
	
	static void addAuthentication(HttpServletResponse response, String username) {
		String JWT = Jwts.builder()
				.setSubject(username)
				.setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
				.signWith(SignatureAlgorithm.HS512, SECRET)
                .compact();
        
        response.addHeader("Content-Type", "application/json");
        
        try {
            response.getWriter().write(String.format("{ \"token\": \"%s\" }", JWT));
            response.getWriter().flush();
            response.getWriter().close();
        } catch (IOException e) {
            e.printStackTrace();
		}
	}
	
	public String generateJwtToken(Authentication auth) {
		//String username = ((User)auth.getPrincipal()).getUsername();
		String username = auth.getPrincipal().toString();
		System.out.println(username);
		logger.info("To generate token for user: " + username);
		String JWT = Jwts.builder()
				.setSubject(username)
				.setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
				.signWith(SignatureAlgorithm.HS512, SECRET)
                .compact();
		return JWT;
	}
	
	public String getUsernameFromToken(String token) {
		String user = Jwts.parser()
				.setSigningKey(SECRET)
				.parseClaimsJws(token)
				.getBody()
				.getSubject();
		return user;
	}
	
	static Authentication getAuthentication(HttpServletRequest request) {
		String token = request.getHeader(HEADER_STRING);
		try {
		if (token != null) {
			String user = Jwts.parser()
					.setSigningKey(SECRET)
					.parseClaimsJws(token.replace(TOKEN_PREFIX, ""))
					.getBody()
					.getSubject();
			
			if (user != null) {
				return new UsernamePasswordAuthenticationToken(user, null, Collections.emptyList());
			}
		}
		}catch(MalformedJwtException ex) {
			logger.error("TOKEN ERROR Exception: " + ex.getMessage());
		}catch(Exception ex) {
			logger.error("TOKEN ERROR: " + ex.getMessage());
		}
		return null;
	}
	
}
