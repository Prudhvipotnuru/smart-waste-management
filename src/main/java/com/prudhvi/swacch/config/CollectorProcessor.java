package com.prudhvi.swacch.config;

import org.apache.commons.lang3.RandomStringUtils;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.prudhvi.swacch.model.CollectorCredential;
import com.prudhvi.swacch.model.User;
import com.prudhvi.swacch.model.UserRole;
import com.prudhvi.swacch.repos.CollectorCredentialRepo;
import com.prudhvi.swacch.repos.UserRepo;

public class CollectorProcessor implements ItemProcessor<User,User>{
	
	public static final Logger log = LoggerFactory.getLogger(CollectorProcessor.class);
	
	private final UserRepo uRepo;
    private final PasswordEncoder passwordEncoder;
    private final CollectorCredentialRepo cRepo;
    private final Long jobExecutionId;
	
	public CollectorProcessor(UserRepo uRepo, PasswordEncoder passwordEncoder, CollectorCredentialRepo cRepo,
			Long jobExecutionId) {
		this.uRepo = uRepo;
		this.passwordEncoder = passwordEncoder;
		this.cRepo = cRepo;
		this.jobExecutionId = jobExecutionId;
	}

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
            String password = RandomStringUtils.randomAlphanumeric(8);
        	String hashedPassword=passwordEncoder.encode(password);
        	log.debug(password+" Hashed Password: "+hashedPassword);
        	collector.setPassword(hashedPassword);
        	collector.setTempPassword(password);
        	cRepo.save(new CollectorCredential(
        			collector.getName(),
        			collector.getEmail(),
        			collector.getPhone(),
        			password,
        			jobExecutionId
        			));
        }
        return collector;
	}

}
