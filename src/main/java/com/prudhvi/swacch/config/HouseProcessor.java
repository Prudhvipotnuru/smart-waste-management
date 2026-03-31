package com.prudhvi.swacch.config;

import java.util.UUID;

import org.jspecify.annotations.Nullable;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;

import com.prudhvi.swacch.model.House;
import com.prudhvi.swacch.repos.HouseRepo;

public class HouseProcessor implements ItemProcessor<House, House>{
	
	@Autowired
	private HouseRepo hrepo;

	@Override
	public @Nullable House process(House house) throws Exception {
		StringBuilder errors = new StringBuilder();

        if (house.getHouseNumber() == null || house.getHouseNumber().isEmpty()) {
            errors.append("House Number is missing; ");
        }else if(hrepo.existsByHouseNumber(house.getHouseNumber())) {
        	errors.append("House with this house number already exists");
        }
        if (house.getOwnerName() == null || house.getOwnerName().isEmpty()) {
            errors.append("Owner Name is missing; ");
        }
        if (house.getAddress() == null || house.getAddress().isEmpty()) {
            errors.append("Address is missing; ");
        }
        if (house.getWard() == null || house.getWard().isEmpty()) {
            errors.append("Ward is missing; ");
        }

        if (errors.length() > 0) {
            house.setError(true);
            house.setErrorDesc(errors.toString());
        } else {
            house.setError(false);
        }
        if(!house.isError())
        	house.setQrCodeValue("H-"+ house.getHouseNumber());
		return house;
	}

}
