package com.waya.wayaauthenticationservice.config;

import com.waya.wayaauthenticationservice.enums.ERole;
import com.waya.wayaauthenticationservice.security.*;
import com.waya.wayaauthenticationservice.security.oauth2.HttpCookieOAuth2AuthorizationRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.SecurityExpressionHandler;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.access.expression.DefaultWebSecurityExpressionHandler;
import org.springframework.security.web.firewall.DefaultHttpFirewall;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;

@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

	@Autowired
	UserPrincipalDetailsService userService;

	@Autowired
	BCryptPasswordEncoder bCryptPasswordEncoder;

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
		httpSecurity.cors().and().csrf().disable().exceptionHandling()
				.authenticationEntryPoint(getBasicAuthEntryPoint()).and().sessionManagement()
				.sessionCreationPolicy(SessionCreationPolicy.STATELESS).and().authorizeRequests()
				.expressionHandler(webSecurityExpressionHandler())
				.antMatchers("/api/v1/auth/login", "/api/v1/password/change**","/api/v1/auth/create/passcode").permitAll()
				.antMatchers("/api/v1/auth/create", "/api/v1/auth/create-corporate","/api/v1/auth/login/passcode").permitAll()
				.antMatchers("/v2/api-docs", "/configuration/**", "/swagger*/**", "/webjars/**").permitAll()
				.antMatchers("/api/v1/auth/resend-otp**/**", "/api/v1/auth/verify-otp", "/api/v1/auth/generate-otp/**","/api/v1/auth/verify-otp/transaction").permitAll()
				.antMatchers("/api/v1/business/type/find/all", "/api/v1/password/forgot-password").permitAll()
				.antMatchers("/api/v1/auth/verify**", "/api/v1/password/forgot-password**/**").permitAll()
				.antMatchers("/api/v1/password/change-password**/**", "/api/v1/user/users", "/api/v1/auth/wallet/**").permitAll()
				.antMatchers("/api/v1/utils/**", "/api/v1/fraud-actions/**", "/auth-service/**","/api/v1/auth/validate-user").permitAll()

				// all other requests need to be authenticated
				.anyRequest().authenticated().and()
				// make sure we use stateLess session; session won't be used to
				// store user's state.
				.addFilter(getAuthenticationFilter())
				.addFilter(new AuthorizationFilter(authenticationManager()));
	}

	@Override
	public void configure(WebSecurity web) {
		web.ignoring().antMatchers("/v2/api-docs", "/configuration/ui", "/swagger-resources/**",
				"/configuration/security", "/swagger-ui/index.html","/auth-service/**", "/webjars/**");
	}

	protected AuthenticationFilter getAuthenticationFilter() throws Exception {
		final AuthenticationFilter filter = new AuthenticationFilter(authenticationManager());
		filter.setFilterProcessesUrl("/api/v1/auth/login");
		return filter;
		// JwtRequestFilter filter = new JwtRequestFilter(authenticationManager());
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		final org.springframework.web.cors.CorsConfiguration configuration = new CorsConfiguration();

		configuration.setAllowedOrigins(Collections.singletonList("*"));
		configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
		configuration.setAllowCredentials(true);
		configuration.setAllowedHeaders(Collections.singletonList("*"));

		final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);

		return source;
	}

	@Bean
	public RoleHierarchyImpl roleHierarchy() {
		RoleHierarchyImpl roleHierarchy = new RoleHierarchyImpl();
		roleHierarchy.setHierarchy(ERole.getRoleHierarchy());
		return roleHierarchy;
	}

	private SecurityExpressionHandler<FilterInvocation> webSecurityExpressionHandler() {
		DefaultWebSecurityExpressionHandler defaultWebSecurityExpressionHandler = new DefaultWebSecurityExpressionHandler();
		defaultWebSecurityExpressionHandler.setRoleHierarchy(roleHierarchy());
		return defaultWebSecurityExpressionHandler;
	}

	@Bean
	public HttpFirewall defaultHttpFirewall() {
		return new DefaultHttpFirewall();
	}

	@Bean
	public HttpCookieOAuth2AuthorizationRequestRepository cookieAuthorizationRequestRepository() {
		return new HttpCookieOAuth2AuthorizationRequestRepository();
	}

	@Bean
	public JwtAuthenticationEntryPoint getBasicAuthEntryPoint() {
		return new JwtAuthenticationEntryPoint();
	}

	@Bean
	public AccessDeniedHandler accessDeniedHandler() {
		return new CustomAccessDeniedHandler();
	}
}
