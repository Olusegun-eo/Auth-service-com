package com.waya.wayaauthenticationservice.util;

import static com.waya.wayaauthenticationservice.util.SecurityConstants.getSecret;

import java.security.Key;
import java.util.Date;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtUtil {

	@Value("${jwt.secret}")
	private String secret;

	private Key key;

	/*@PostConstruct
	public void init() {
		this.key = Keys.hmacShaKeyFor(getSecret().getBytes());
	}*/

	private Key getSigningKey() {
		byte[] keyBytes = getSecret().getBytes();
		return Keys.hmacShaKeyFor(keyBytes);
	}

	public Claims getAllClaimsFromToken(String token) {
		key = getSigningKey();
		return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
	}

	private boolean isTokenExpired(String token) {
		return this.getAllClaimsFromToken(token).getExpiration().before(new Date());
	}

	public boolean isInvalid(String token) {
		return this.isTokenExpired(token);
	}

	public String doGenerateToken(Map<String, Object> claims, String username, Date expirationDate) {

		final Date createdDate = new Date();
		key = getSigningKey();
		return Jwts.builder().setClaims(claims).setSubject(username).setIssuedAt(createdDate)
				.setExpiration(expirationDate).signWith(key).compact();
	}

}
