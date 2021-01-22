package com.waya.wayaauthenticationservice.security;

import com.waya.wayaauthenticationservice.SpringApplicationContext;
import com.waya.wayaauthenticationservice.entity.Roles;
import com.waya.wayaauthenticationservice.entity.Users;
import com.waya.wayaauthenticationservice.repository.UserRepository;
import com.waya.wayaauthenticationservice.util.SecurityConstants;
import io.jsonwebtoken.Jwts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class AuthorizationFilter extends BasicAuthenticationFilter {

    private static final Logger LOGGER= LoggerFactory.getLogger(AuthorizationFilter.class);
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
        String authorizationHeader = request.getHeader(SecurityConstants.HEADER_STRING);
        if (authorizationHeader == null) {
            return null;
        }

        String token = authorizationHeader.replace(SecurityConstants.TOKEN_PREFIX, "");

        String userId = Jwts.parser().setSigningKey(SecurityConstants.getSecret()).parseClaimsJws(token).getBody()
                .getSubject();

        //LOGGER.info("Role Auth::: {}, {} and {}", userId,2,3);
        if (userId == null) {
            return null;
        }
        UserRepository userLoginRepo = (UserRepository) SpringApplicationContext.getBean("usersRepository");
        Users user = userLoginRepo.findByEmail(userId).get();
        List<Roles> roles = user.getRolesList();
        List<GrantedAuthority> grantedAuthorities = roles.stream().map(r -> {
            //LOGGER.info("Role::: {}, {} and {}", r.getName(),2,3);
            return new SimpleGrantedAuthority(r.getName());
        }).collect(Collectors.toList());
        return new UsernamePasswordAuthenticationToken(userId, null,grantedAuthorities);


    }
}
