package com.prudhvi.swacch.config;

import org.jspecify.annotations.Nullable;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.prudhvi.swacch.model.User;
import com.prudhvi.swacch.model.UserRole;
import com.prudhvi.swacch.repos.UserRepo;

public class CollectorProcessor implements ItemProcessor<User,User>{
	
	@Autowired
	private UserRepo uRepo;
	
	@Autowired
	private PasswordEncoder passwordEncoder;

	@Override
	public @Nullable User process(User collector) throws Exception {
		StringBuilder errors = new StringBuilder();

        if (collector.getName() == null || collector.getName().isEmpty()) {
            errors.append("User name is missing; ");
        }
        if (collector.getRole() == null) {
            errors.append("User Role is missing; ");
        } else if(!collector.getRole().equals(UserRole.COLLECTOR)) {
        	errors.append("User Role should be COLLECTOR; ");
        }
        if (collector.getPhone() == null || collector.getPhone().isEmpty()) {
            errors.append("Phone number is missing; ");
        } else if(uRepo.existsByPhone(collector.getPhone())) {
        	errors.append("Phone number is already registered by another user; ");
        }
        if (collector.getEmail() == null || collector.getEmail().isEmpty()) {
            errors.append("Email is missing; ");
        } else if(uRepo.existsByEmail(collector.getEmail())) {
        	errors.append("Email ID is already registered by another user; ");
        }

        if (errors.length() > 0) {
            collector.setError(true);
            collector.setErrorDesc(errors.toString());
        } else {
            collector.setError(false);
            String password="password";
        	String hashedPassword=passwordEncoder.encode(password);
        	System.out.println(password+" Hashed Password: "+hashedPassword);
        	collector.setPassword(hashedPassword);
        	collector.setTempPassword(password);
        }
        return collector;
	}

}
