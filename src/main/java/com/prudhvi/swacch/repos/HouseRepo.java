package com.prudhvi.swacch.repos;

import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.prudhvi.swacch.model.House;

public interface HouseRepo extends JpaRepository<House, Long> {

	boolean existsByHouseNumber(String houseNumber);

	Optional<House> findByQrCodeValue(String houseqr);

	@Query("select h.houseNumber from House h")
	Set<String> findAllHouseNumbers();
}
