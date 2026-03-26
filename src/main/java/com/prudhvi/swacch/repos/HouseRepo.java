package com.prudhvi.swacch.repos;

import org.springframework.data.jpa.repository.JpaRepository;
import com.prudhvi.swacch.model.House;

public interface HouseRepo extends JpaRepository<House, Long> {

	boolean existsByHouseNumber(String houseNumber);

}
