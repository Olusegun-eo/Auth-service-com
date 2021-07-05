package com.waya.wayaauthenticationservice.security;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.waya.wayaauthenticationservice.SpringApplicationContext;
import com.waya.wayaauthenticationservice.entity.Roles;
import com.waya.wayaauthenticationservice.entity.Users;
import com.waya.wayaauthenticationservice.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
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
        String userToken = null;
        Collection<GrantedAuthority> grantedAuthorities = null;
        if (token != null) {

            token = token.replace(SecurityConstants.TOKEN_PREFIX, "");

            userToken = Jwts.parser().setSigningKey(SecurityConstants.getSecret()).parseClaimsJws(token).getBody()
                    .getSubject();
            UserRepository userLoginRepo = (UserRepository) SpringApplicationContext.getBean("userRepository");

            Users user = userLoginRepo.findByEmailOrPhoneNumber(userToken,userToken)
                                      .orElseThrow(() -> new BadCredentialsException("User Does not exist"));
            if (user != null) {
                List<Roles> roles = new ArrayList<>(user.getRolesList());
                grantedAuthorities = roles.stream().map(r -> new SimpleGrantedAuthority(r.getName())).collect(Collectors.toSet());
                grantedAuthorities.addAll(getGrantedAuthorities(getPrivileges(roles)));

                return new UsernamePasswordAuthenticationToken(userToken, null, grantedAuthorities);
            }
            return null;
        }
        return new UsernamePasswordAuthenticationToken(null, null,null);
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
