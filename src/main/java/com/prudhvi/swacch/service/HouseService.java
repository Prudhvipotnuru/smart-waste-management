package com.prudhvi.swacch.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import com.prudhvi.swacch.dtos.HouseRequest;
import com.prudhvi.swacch.dtos.HouseResponse;
import com.prudhvi.swacch.model.House;
import com.prudhvi.swacch.repos.HouseRepo;

@Service
public class HouseService {
	
	private HouseRepo repo;
	
	HouseService(HouseRepo repo){
		this.repo=repo;
	}

	public HouseResponse save(HouseRequest request) {
		House house=new House();
		BeanUtils.copyProperties(request, house);
		house.setQrCodeValue("H:"+ request.getHouseNumber());
		House resp = repo.save(house);
		HouseResponse response=new HouseResponse();
		BeanUtils.copyProperties(resp, response);
		return response;
	}

	public List<HouseResponse> getAllHouses() {
		List<House> houses = repo.findAll();
		List<HouseResponse> response=new ArrayList<HouseResponse>();
		for(House house:houses) {
			HouseResponse resp=new HouseResponse();
			BeanUtils.copyProperties(house, resp);
			response.add(resp);
		}
		return response;
	}

	public HouseResponse getHouse(Long id) {
		Optional<House> house = repo.findById(id);
		HouseResponse resp=new HouseResponse();
		if(house.isPresent()) {
			BeanUtils.copyProperties(house.get(), resp);
		}
		return resp;
	}

}
