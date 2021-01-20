package com.waya.wayaauthenticationservice.config;

import com.waya.wayaauthenticationservice.security.AuthenticationFilter;
import com.waya.wayaauthenticationservice.security.AuthorizationFilter;
import com.waya.wayaauthenticationservice.security.JwtAuthenticationEntryPoint;
import com.waya.wayaauthenticationservice.service.UserService;
import com.waya.wayaauthenticationservice.service.impl.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.firewall.DefaultHttpFirewall;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Autowired
    private UserService userService;


    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        // configure AuthenticationManager so that it knows from where to load
        // user for matching credentials
        // Use BCryptPasswordEncoder
        auth.userDetailsService(userService).passwordEncoder(bCryptPasswordEncoder);
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception {

        httpSecurity.
                cors().and().csrf().disable()
                // dont authenticate this particular request
                .authorizeRequests().antMatchers("/api/auth/create","/api/auth/login").permitAll()
                .antMatchers("/v2/api-docs", "/configuration/**", "/swagger*/**", "/webjars/**").permitAll()
                // all other requests need to be authenticated
                .anyRequest().authenticated().and()
                // make sure we use stateless session; session won't be used to
                // store user's state.
                .addFilter(getAuthenticationFilter())
                .addFilter(new AuthorizationFilter(authenticationManager())).sessionManagement()

                .sessionCreationPolicy(SessionCreationPolicy.STATELESS);

    }


    protected AuthenticationFilter getAuthenticationFilter() throws Exception {
        //JwtRequestFilter filter = new JwtRequestFilter(authenticationManager());
        final AuthenticationFilter filter = new AuthenticationFilter(authenticationManager());
        filter.setFilterProcessesUrl("/api/auth/login");
        return filter;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        final org.springframework.web.cors.CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowCredentials(true);
        configuration.setAllowedHeaders(Arrays.asList("*"));

        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    @Bean
    public HttpFirewall defaultHttpFirewall() {
        return new DefaultHttpFirewall();
    }
}
