package com.waya.wayaauthenticationservice.security;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.waya.wayaauthenticationservice.util.SecurityConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Serializable;

@SuppressWarnings("serial")
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint, Serializable { 
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationEntryPoint.class);

    @Override
    public void commence(HttpServletRequest req,
                         HttpServletResponse httpServletResponse,
                         AuthenticationException e) throws IOException, ServletException {
       
        String tokenPassed = req.getHeader(SecurityConstants.HEADER_STRING);
        String message = e.getMessage() + ":: Attempt to access the protected URL: " + req.getRequestURI()
                + " is Denied.";

        logger.error("Message - {} :: token Passed is" +
                " {}", message, tokenPassed);
        
        httpServletResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED,
                message);
    }
}
