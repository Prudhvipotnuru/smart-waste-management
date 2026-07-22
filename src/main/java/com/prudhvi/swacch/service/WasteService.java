package com.prudhvi.swacch.service;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.apache.coyote.BadRequestException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
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
	
	private WasteCollectionResponse processWasteResponse(WasteCollection waste) {
		WasteCollectionResponse response=new WasteCollectionResponse();
		response.setCollectedAt(waste.getCollectedAt());
		response.setCollectorId(waste.getCollector().getId());
		response.setCollectorName(waste.getCollector().getName());
		response.setHouseId(waste.getHouse().getId());
		response.setHouseNumber(waste.getHouse().getHouseNumber());
		response.setLatitude(waste.getLatitude());
		response.setLongitude(waste.getLongitude());
		response.setSegregationStatus(waste.getSegregationStatus());
		response.setPhotoPath(waste.getPhotoPath());
		return response;
	}
	
	public WasteCollectionResponse save(WasteCollectionRequest request,Authentication auth) throws BadRequestException {
		WasteCollection waste=new WasteCollection();
		waste.setSegregationStatus(request.getSegregationStatus());
		waste.setPhotoPath(request.getPhotoPath());
		waste.setLatitude(request.getLatitude());
		waste.setLongitude(request.getLongitude());
		
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
	
	private String savePhoto(String base64Photo, Long houseId) throws BadRequestException{
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
			throw new BadRequestException("Failed to save Photo",e);
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

	public Page<WasteCollectionResponse> getWasteByCollectorIdAndCurrentDate(Authentication auth, int page,
	        int size,
	        //@RequestParam(defaultValue = "collectedAt,desc") String sort,  // e.g. "houseNumber,asc"
	        String status                   // SEGREGATED / NOT_SEGREGATED
	        ,String date
	        ) throws BadRequestException{
		Long userId = urepo.findIdByName(auth.getName());
		if(userId==null) {
			throw new EntityNotFoundException("User Not Found");
		}
		PageRequest pageable=PageRequest.of(page, size);
		Page<WasteCollection> wasteCollections;
		boolean hasDate = date != null && !date.isBlank();
		if(status !=null && !status.isBlank()) {
			SegregationStatus segStatus;
			try {
			    segStatus = SegregationStatus.valueOf(status.toUpperCase());
			} catch (IllegalArgumentException e) {
			    throw new BadRequestException("Invalid segregation status");
			}
			if(hasDate) {
				wasteCollections = wrepo.findByCollectorIdAndSegregationStatus(userId,pageable,segStatus);
			}else {
				wasteCollections = wrepo.findByCollectorIdAndSegregationStatusAndCollectedAtBetween(userId,pageable,segStatus,
						LocalDate.parse(date).atStartOfDay(),LocalDate.parse(date).plusDays(1).atStartOfDay());
			}
		}
		else {
			if(hasDate) {
				wasteCollections = wrepo.findByCollectorIdAndCollectedAtBetween(userId,pageable,LocalDate.parse(date).atStartOfDay(),LocalDate.parse(date).plusDays(1).atStartOfDay());
			}else {
				wasteCollections = wrepo.findByCollectorId(userId,pageable);
			}
		}
		return wasteCollections.map(this::processWasteResponse);
	}

	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public Page<WasteCollectionResponse> getAllCollections(Authentication auth, int page, int size, String status,
			String date) throws BadRequestException {
		PageRequest pageable=PageRequest.of(page, size);
		Page<WasteCollection> wasteCollections;
		boolean hasDate = date != null && !date.isBlank();
		if(status !=null && !status.isBlank()) {
			SegregationStatus segStatus;
			try {
			    segStatus = SegregationStatus.valueOf(status.toUpperCase());
			} catch (IllegalArgumentException e) {
			    throw new BadRequestException("Invalid segregation status");
			}
			
			if(hasDate) {
				wasteCollections = wrepo.findBySegregationStatusAndCollectedAtBetween(pageable,segStatus,
						LocalDate.parse(date).atStartOfDay(),LocalDate.parse(date).plusDays(1).atStartOfDay());
			}else {
				wasteCollections = wrepo.findBySegregationStatus(pageable,segStatus);
			}
		}
		else {
			if(hasDate) {
				wasteCollections = wrepo.findByCollectedAtBetween(pageable,LocalDate.parse(date).atStartOfDay(),LocalDate.parse(date).plusDays(1).atStartOfDay());
			}else {
				wasteCollections = wrepo.findAll(pageable);
			}
		}
		return wasteCollections.map(this::processWasteResponse);
	}

}
