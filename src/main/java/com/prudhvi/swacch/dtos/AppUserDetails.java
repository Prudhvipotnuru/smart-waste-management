package com.prudhvi.swacch.dtos;

import java.util.Collection;
import java.util.List;

import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.prudhvi.swacch.model.User;

public class AppUserDetails implements UserDetails{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String password;
	private String username;
	private String phone;
	private String role;
	
	public AppUserDetails(User user) {
		this.password = user.getPassword();
		this.role = user.getRole().name();
		this.username = user.getName();
		this.phone = user.getPhone();
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		// TODO Auto-generated method stub
		return List.of(new SimpleGrantedAuthority("ROLE_" + role));
	}

	@Override
	public @Nullable String getPassword() {
		return password;
	}

	@Override
	public String getUsername() {
		return username;
	}
	
	public String getPhone() {
		return phone;
	}

}
