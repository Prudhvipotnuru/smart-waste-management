package com.prudhvi.swacch.service;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.prudhvi.swacch.dtos.AppUserDetails;
import com.prudhvi.swacch.model.User;
import com.prudhvi.swacch.repos.UserRepo;

@Service
public class AppUserDetailsService implements UserDetailsService {

	private UserRepo urepo;

	public AppUserDetailsService(UserRepo urepo) {
		this.urepo = urepo;
	}

	@Override
	public AppUserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User user = urepo.findByEmailOrName(username,username);
		if (user==null) {
			throw new UsernameNotFoundException("User not found");
		}
		return new AppUserDetails(user);
	}

}
