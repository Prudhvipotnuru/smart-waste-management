package com.prudhvi.swacch.repos;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import com.prudhvi.swacch.model.User;
import com.prudhvi.swacch.model.UserRole;

@RepositoryRestResource(path = "user")
public interface UserRepo extends JpaRepository<User, Long> {
	Optional<User> findByName(String name);

	boolean existsByPhone(String phone);

	boolean existsByEmail(String email);

	List<User> findByRoleAndPasswordChangedFalse(UserRole collector);

	Optional<User> findByEmail(String email);

	Optional<User> findByPhone(String phone);
}
