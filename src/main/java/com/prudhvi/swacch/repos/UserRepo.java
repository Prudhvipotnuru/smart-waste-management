package com.prudhvi.swacch.repos;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import com.prudhvi.swacch.model.User;

@RepositoryRestResource(path = "user")
public interface UserRepo extends JpaRepository<User, Long> {
	Optional<User> findByName(String name);
}
