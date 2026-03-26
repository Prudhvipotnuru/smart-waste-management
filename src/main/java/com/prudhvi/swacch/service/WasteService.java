package com.prudhvi.swacch.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import com.prudhvi.swacch.dtos.DashBoardResponse;
import com.prudhvi.swacch.dtos.WasteCollectionRequest;
import com.prudhvi.swacch.dtos.WasteCollectionResponse;
import com.prudhvi.swacch.model.House;
import com.prudhvi.swacch.model.SegregationStatus;
import com.prudhvi.swacch.model.User;
import com.prudhvi.swacch.model.UserRole;
import com.prudhvi.swacch.model.WasteCollection;
import com.prudhvi.swacch.repos.HouseRepo;
import com.prudhvi.swacch.repos.UserRepo;
import com.prudhvi.swacch.repos.WasteCollectionRepo;

import jakarta.persistence.EntityNotFoundException;

@Service
public class WasteService {
	
	private WasteCollectionRepo wrepo;
	private UserRepo urepo;
	private HouseRepo hrepo;
	
	WasteService(WasteCollectionRepo repo,UserRepo urepo,HouseRepo hrepo){
		this.wrepo=repo;
		this.urepo=urepo;
		this.hrepo=hrepo;
	}

	private List<WasteCollectionResponse> processWasteList(List<WasteCollection> list) {
		List<WasteCollectionResponse> response= new ArrayList<WasteCollectionResponse>();
		for(WasteCollection waste:list) {
			WasteCollectionResponse resp=processWasteResponse(waste);
			response.add(resp);
		}
		return response;
	}
	
	private WasteCollectionResponse processWasteResponse(WasteCollection resp) {
		WasteCollectionResponse response=new WasteCollectionResponse();
		BeanUtils.copyProperties(resp, response);
		response.setCollectorId(resp.getCollector().getId());
		response.setCollectorName(resp.getCollector().getName());
		response.setHouseId(resp.getHouse().getId());
		response.setHouseNumber(resp.getHouse().getHouseNumber());
		return response;
	}
	
	public WasteCollectionResponse save(WasteCollectionRequest request) {
		WasteCollection waste=new WasteCollection();
		BeanUtils.copyProperties(request, waste);
		
		Optional<User> user = urepo.findById(request.getCollectorId());
		if(user.isPresent()) {
			if(!UserRole.COLLECTOR.equals(user.get().getRole())) {
				throw new EntityNotFoundException("Role of the User should be Collector");
			}
			waste.setCollector(user.get());
		}else {
			throw new EntityNotFoundException("User Not Found");
		}
		
		Optional<House> house = hrepo.findById(request.getHouseId());
		if(house.isPresent()) {
			waste.setHouse(house.get());
		}else {
			throw new EntityNotFoundException("House Not Found");
		}
		
		WasteCollection resp = wrepo.save(waste);
		WasteCollectionResponse response = processWasteResponse(resp);
		return response;
	}


	public List<WasteCollectionResponse> getAll() {
		List<WasteCollection> list = wrepo.findAll();
		return processWasteList(list);
	}

	public List<WasteCollectionResponse> getWasteByHouseId(Long id) {
		List<WasteCollection> list = wrepo.findByHouseId(id);
		return processWasteList(list);
	}

	public List<WasteCollectionResponse> getWasteByCollectorId(Long id) {
		List<WasteCollection> list = wrepo.findByCollectorId(id);
		return processWasteList(list);
	}
	
	public DashBoardResponse dashboard() {
		DashBoardResponse response=new DashBoardResponse();
		response.setHouseCount(hrepo.count());
		response.setWasteCount(wrepo.count());
		response.setSegregatedCount(wrepo.countBySegregationStatus(SegregationStatus.SEGREGATED));
		response.setNonSegregatedCount(wrepo.countBySegregationStatus(SegregationStatus.NOT_SEGREGATED));
		return response;
	}

}
