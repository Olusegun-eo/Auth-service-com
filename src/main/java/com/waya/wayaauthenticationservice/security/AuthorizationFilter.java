package com.waya.wayaauthenticationservice.security;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.util.StringUtils;

import com.waya.wayaauthenticationservice.SpringApplicationContext;
import com.waya.wayaauthenticationservice.entity.Users;
import com.waya.wayaauthenticationservice.repository.UserRepository;
import com.waya.wayaauthenticationservice.util.SecurityConstants;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;


public class AuthorizationFilter extends BasicAuthenticationFilter {

    //private static final Logger LOGGER= LoggerFactory.getLogger(AuthorizationFilter.class);
    
    public AuthorizationFilter(AuthenticationManager authManager) {
        super(authManager);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws IOException, ServletException {
    	
    	System.out.println(":::::REQUEST:::::"+req.getAuthType());
        String header = req.getHeader(SecurityConstants.HEADER_STRING);

        System.out.println("::::::::::auth::::::::");
        if (header == null || !header.startsWith(SecurityConstants.TOKEN_PREFIX)) {
            chain.doFilter(req, res);
            return;
        }
        UsernamePasswordAuthenticationToken authentication = getAuthentication(req);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        chain.doFilter(req, res);
    }

    private UsernamePasswordAuthenticationToken getAuthentication(HttpServletRequest request) {
    	System.out.println(":::::::Authorization filter:::::");
        String token = parseJwt(request);
        String userToken;
        if (token != null && validateToken(token)) {
            userToken = getUserNameFromToken(token);
            UserRepository userLoginRepo = (UserRepository) SpringApplicationContext.getBean("userRepository");

            Users user = userLoginRepo.findByEmailOrPhoneNumber(userToken).orElse(null);
                        // .orElseThrow(() -> new BadCredentialsException("User Does not exist"));
            if (user != null) {
                UserPrincipal userPrincipal = new UserPrincipal(user);
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userPrincipal, null, userPrincipal.getAuthorities());

                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);

                return authentication;
            }
            return null;
        }
        return new UsernamePasswordAuthenticationToken(null, null,null);
    }

    private boolean validateToken(String authToken) {
        try {
            Jwts.parser().setSigningKey(SecurityConstants.getSecret()).parseClaimsJws(authToken);
            return true;
        } catch (SignatureException ex) {
            logger.error("Invalid JWT signature");
        } catch (MalformedJwtException ex) {
            logger.error("Invalid JWT token");
        } catch (ExpiredJwtException ex) {
            logger.error("Expired JWT token");
        } catch (UnsupportedJwtException ex) {
            logger.error("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            logger.error("JWT claims string is empty.");
        }
        return false;
    }

    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader(SecurityConstants.HEADER_STRING);

        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith(SecurityConstants.TOKEN_PREFIX)) {
            return  headerAuth.replace(SecurityConstants.TOKEN_PREFIX, "");
        }
        return null;
    }

    private String getUserNameFromToken(String token) {
        return Jwts.parser().setSigningKey(SecurityConstants.getSecret()).parseClaimsJws(token).getBody().getSubject();
    }
}
