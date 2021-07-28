package com.waya.wayaauthenticationservice.security;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse res,
                       AccessDeniedException ex) throws IOException, ServletException {

        PrintWriter pr = res.getWriter();
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");

        Map<String, Object> response = new HashMap<>();
        response.put("message", ex.getMessage());
        response.put("status", false);
        response.put("timestamp", new Date());

        String str ="";
        Authentication auth
                = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            str = "User: " + auth.getName()
                    + " attempt to the protected URL: "
                    + request.getRequestURI() + " is Denied.";
            response.put("data", str);
        }
        String json = new Gson().toJson(response);
        pr.write(json);
    }
}
