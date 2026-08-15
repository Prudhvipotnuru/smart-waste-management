package com.prudhvi.swacch.config;

import java.util.Set;

import org.jspecify.annotations.Nullable;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.batch.infrastructure.item.ItemStream;

import com.prudhvi.swacch.model.House;
import com.prudhvi.swacch.repos.HouseRepo;

public class HouseProcessor implements ItemProcessor<House, House>,ItemStream{
	
	private final HouseRepo hrepo;
	private Set<String> existingHouseNumbers;
	
	public HouseProcessor(HouseRepo hrepo) {
		this.hrepo=hrepo;
	}
	
	@Override
	public void open(ExecutionContext context) {
		existingHouseNumbers=hrepo.findAllHouseNumbers();
	}
	
	@Override
	public @Nullable House process(House house) throws Exception {
		StringBuilder errors = new StringBuilder();

        if (house.getHouseNumber() == null || house.getHouseNumber().isEmpty()) {
            errors.append("House Number is missing; ");
        } else {
        	if(existingHouseNumbers.contains(house.getHouseNumber())) {
        		errors.append("House Number already exists");
        	}
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
