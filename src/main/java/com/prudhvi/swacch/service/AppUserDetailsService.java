package com.prudhvi.swacch.service;

import java.util.Optional;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.prudhvi.swacch.model.User;
import com.prudhvi.swacch.repos.UserRepo;

@Service
public class AppUserDetailsService implements UserDetailsService {

	private UserRepo urepo;

	public AppUserDetailsService(UserRepo urepo) {
		this.urepo = urepo;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		Optional<User> byName = urepo.findByName(username);
		if (byName.isEmpty()) {
			throw new UsernameNotFoundException("User not found");
		}
		User user = byName.get();
		return org.springframework.security.core.userdetails.User.builder().username(user.getName())
				.password(user.getPassword()).roles(user.getRole().toString()).build();
	}

}
