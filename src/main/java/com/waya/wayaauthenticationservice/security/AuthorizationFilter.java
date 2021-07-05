package com.waya.wayaauthenticationservice.security;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import com.waya.wayaauthenticationservice.util.SecurityConstants;

import io.jsonwebtoken.Jwts;


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
        String token = request.getHeader(SecurityConstants.HEADER_STRING);
        String user = null;
        if (token != null) {

            token = token.replace(SecurityConstants.TOKEN_PREFIX, "");

            user = Jwts.parser().setSigningKey(SecurityConstants.getSecret()).parseClaimsJws(token).getBody()
                    .getSubject();

            if (user != null) {
                return new UsernamePasswordAuthenticationToken(user, null, new ArrayList<>());
            }
            return null;
        }

        return new UsernamePasswordAuthenticationToken(user, null,null);

    }
}
