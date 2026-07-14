package com.prudhvi.swacch.service;

import java.util.Optional;

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
	public AppUserDetails loadUserByUsername(String phone) throws UsernameNotFoundException {
		Optional<User> byName = urepo.findByPhone(phone);
		if (byName.isEmpty()) {
			throw new UsernameNotFoundException("User not found");
		}
		User user = byName.get();
		return new AppUserDetails(user);
	}

}
