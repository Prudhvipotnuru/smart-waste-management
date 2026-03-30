package com.prudhvi.swacch.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.security.core.Authentication;
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
	
	public WasteCollectionResponse save(WasteCollectionRequest request,Authentication auth) {
		WasteCollection waste=new WasteCollection();
		BeanUtils.copyProperties(request, waste);
		
		User user = urepo.findByName(auth.getName()).orElseThrow(()->new EntityNotFoundException("User Not Found"));
		if (!UserRole.COLLECTOR.equals(user.getRole())) {
	        throw new RuntimeException("Only collectors can add waste");
	    }
		waste.setCollector(user);
		
		House house = hrepo.findByQrCodeValue(request.getHouseqr())
	            .orElseThrow(() -> new EntityNotFoundException("House not found"));

	    waste.setHouse(house);
	    
	 // Optional: save photo if provided
        if (request.getPhotoPath() != null && !request.getPhotoPath().isEmpty()) {
            String savedPath = savePhoto(request.getPhotoPath(), house.getId());
            waste.setPhotoPath(savedPath);
        }
		
		WasteCollection resp = wrepo.save(waste);
		return processWasteResponse(resp);
	}
	
	private String savePhoto(String base64Photo, Long houseId){
	    // Remove "data:image/png;base64," prefix if exists
	    if (base64Photo.contains(",")) {
	        base64Photo = base64Photo.split(",")[1];
	    }

	    byte[] data = java.util.Base64.getDecoder().decode(base64Photo);

	    String dirPath = System.getProperty("user.dir") + "/uploads/photos";
	    File dir = new File(dirPath);
	    if (!dir.exists()) dir.mkdirs();

	    String fileName = "house_" + houseId + "_" + System.currentTimeMillis() + ".png";
	    File file = new File(dir, fileName);

	    try {
			java.nio.file.Files.write(file.toPath(), data);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	    return "/uploads/photos/" + fileName; // can be used in frontend to show image
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
