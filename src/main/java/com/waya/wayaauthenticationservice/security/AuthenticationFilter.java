package com.waya.wayaauthenticationservice.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.waya.wayaauthenticationservice.SpringApplicationContext;
import com.waya.wayaauthenticationservice.entity.Roles;
import com.waya.wayaauthenticationservice.entity.Users;
import org.springframework.security.core.userdetails.User;
import com.waya.wayaauthenticationservice.pojo.LoginDetailsPojo;
import com.waya.wayaauthenticationservice.pojo.UserProfileResponsePojo;
import com.waya.wayaauthenticationservice.repository.UserRepository;
import com.waya.wayaauthenticationservice.util.SecurityConstants;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;

public class AuthenticationFilter extends UsernamePasswordAuthenticationFilter {
    private final Gson gson = new Gson();
    private static final Logger LOGGER= LoggerFactory.getLogger(AuthenticationFilter.class);



    public AuthenticationFilter(AuthenticationManager manager) {
        super.setAuthenticationManager(manager);
    }



    @Override
    public Authentication attemptAuthentication(HttpServletRequest req, HttpServletResponse res)
            throws AuthenticationException {
        try {

            LoginDetailsPojo creds = new ObjectMapper().readValue(req.getInputStream(), LoginDetailsPojo.class);

            UserRepository userLoginRepo = (UserRepository) SpringApplicationContext.getBean("userRepository");

            Users user = userLoginRepo.findByEmail(creds.getEmail()).orElseThrow(() -> new BadCredentialsException("User Does not exist"));

            System.out.println(user.getFirstName());
            List<Roles> roles = user.getRolesList();
            List<GrantedAuthority> grantedAuthorities = roles.stream().map(r -> {

                return new SimpleGrantedAuthority(r.getName());
            }).collect(Collectors.toList());
            return getAuthenticationManager().authenticate(
                    new UsernamePasswordAuthenticationToken(creds.getEmail(), creds.getPassword(), grantedAuthorities));


        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest req, HttpServletResponse res, FilterChain chain,
                                            Authentication auth) throws IOException, ServletException, SignatureException {

        String userName = ((User) auth.getPrincipal()).getUsername();

        String token = Jwts.builder().setSubject(userName)
                .setExpiration(new Date(System.currentTimeMillis() + SecurityConstants.EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS256, SecurityConstants.getSecret()).compact();

        UserRepository userLoginRepo = (UserRepository) SpringApplicationContext.getBean("userRepository");

        Users user = userLoginRepo.findByEmail(userName).get();

        res.addHeader(SecurityConstants.HEADER_STRING, SecurityConstants.TOKEN_PREFIX + token);

        Map m = new HashMap();
        m.put("code", 0);
        m.put("message", "Login successful");
        m.put("token", SecurityConstants.TOKEN_PREFIX + token);
        List<String> roles = new ArrayList<>();

        List<Roles> rs = user.getRolesList();
        for (int i = 0; i < rs.size(); i++) {
            roles.add(rs.get(i).getName());

        }
        m.put("roles", roles);
        UserProfileResponsePojo userp = new ModelMapper().map(user, UserProfileResponsePojo.class);
        userp.setPhoneNumber(user.getPhoneNumber());
        //List<CompanyProfile> companyProfile = companyProfileRepos.findByUserId(user.getId());
        m.put("user", userp);
        String str = gson.toJson(m);
        PrintWriter pr = res.getWriter();
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");

        pr.write(str);

    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.ALL_VALUE);
        Map m = new HashMap();
        m.put("code", -1);
        m.put("message", "unsuccessful, invalid username or password");
        String str = gson.toJson(m);
        PrintWriter pr = response.getWriter();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        pr.write(str);
    }
}
