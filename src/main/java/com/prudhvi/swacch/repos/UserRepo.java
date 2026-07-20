package com.prudhvi.swacch.repos;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import com.prudhvi.swacch.dtos.EmailNameProjection;
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

	User findByEmailOrName(String username, String username2);

	@Query("""
			SELECT u.id
			FROM User u
			WHERE u.name = :name
			""")
	Long findIdByName(@Param("name") String name);

	@Query("""
			SELECT u.email as email,u.name as name
			FROM User u
			WHERE role = :role
			AND passwordChanged = false
			""")
	List<EmailNameProjection> findEmailAndNameByRoleAndPasswordChangedFalse(@Param("role") UserRole collector);
}
