package com.waya.wayaauthenticationservice.security;

import com.waya.wayaauthenticationservice.SpringApplicationContext;
import com.waya.wayaauthenticationservice.entity.Privilege;
import com.waya.wayaauthenticationservice.entity.Role;
import com.waya.wayaauthenticationservice.entity.Users;
import com.waya.wayaauthenticationservice.pojo.access.UserAccessResponse;
import com.waya.wayaauthenticationservice.service.UserService;
import lombok.ToString;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.*;
import java.util.stream.Collectors;

@ToString(exclude = "attributes")
public class UserPrincipal implements OAuth2User, UserDetails {

	private final Users user;
	private Map<String, Object> attributes;
	private UserAccessResponse access;

	public UserPrincipal(Users user) {
		this.user = user;
		this.access = ((UserService) Objects.requireNonNull(SpringApplicationContext.getBean("userServiceImpl")))
				.getAccessResponse(this.user.getId()).getData();
	}

	public static UserPrincipal create(Users user) {
		return new UserPrincipal(user);
	}

	public static UserPrincipal create(Users user, Map<String, Object> attributes) {
		UserPrincipal userPrincipal = UserPrincipal.create(user);
		userPrincipal.setAttributes(attributes);
		return userPrincipal;
	}

	public Long getId() {
		return user.getId();
	}

	@Override
	public String getPassword() {
		return user.getPassword();
	}

	@Override
	public boolean isAccountNonExpired() {
		return this.user.isAccountNonExpired();
	}

	@Override
	public boolean isAccountNonLocked() {
		return this.user.isAccountNonLocked();
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return this.user.getAccountStatus() == 1 && this.user.isCredentialsNonExpired();
		//return true;
	}

	@Override
	public boolean isEnabled() {
		return this.user.isActive();
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {

		List<Role> roles = new ArrayList<>(this.user.getRoleList());

		Collection<GrantedAuthority> grantedAuthorities = roles.stream()
				.map(r -> new SimpleGrantedAuthority(r.getName())).collect(Collectors.toSet());
		grantedAuthorities.addAll(getGrantedAuthorities(getPrivileges(roles)));
		
		if(this.getAccess() != null){
			grantedAuthorities.add(new SimpleGrantedAuthority(this.getAccess().getRoleName()));
			grantedAuthorities.add(new SimpleGrantedAuthority(this.getAccess().getPermissionName()));
		}
		return grantedAuthorities;
	}
	
	/**
	 * @return the access
	 */
	public UserAccessResponse getAccess() {
		return access;
	}

	/**
	 * @param access the access to set
	 */
	public void setAccess(UserAccessResponse access) {
		this.access = access;
	}

	@Override
	public Map<String, Object> getAttributes() {
		return attributes;
	}

	public void setAttributes(Map<String, Object> attributes) {
		this.attributes = attributes;
	}
	
	public Optional<Users> getUser() {
		return Optional.of(this.user);
	}
	
	private Set<String> getPrivileges(final Collection<Role> roles) {
		Set<String> privileges = new HashSet<>();
		for (Role role : roles) {
			privileges.addAll(role.getPrivileges().stream().map(Privilege::getName).collect(Collectors.toSet()));
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

	/**
	 * Returns the name of the authenticated <code>Principal</code>. Never
	 * <code>null</code>.
	 *
	 * @return the name of the authenticated <code>Principal</code>
	 */
	@Override
	public String getName() {
		return user.getEmail() != null ? this.user.getEmail() : this.user.getPhoneNumber();
	}

	/**
	 * Returns the username used to authenticate the user. Cannot return
	 * <code>null</code>.
	 *
	 * @return the username (never <code>null</code>)
	 */
	@Override
	public String getUsername() {
		return user.getEmail() != null ? this.user.getEmail() : this.user.getPhoneNumber();
	}

}
