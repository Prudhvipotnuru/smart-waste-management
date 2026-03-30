package com.prudhvi.swacch.repos;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.prudhvi.swacch.model.House;

public interface HouseRepo extends JpaRepository<House, Long> {

	boolean existsByHouseNumber(String houseNumber);

	Optional<House> findByQrCodeValue(String houseqr);

}
