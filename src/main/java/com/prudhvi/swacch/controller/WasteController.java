package com.prudhvi.swacch.controller;

import org.apache.coyote.BadRequestException;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.prudhvi.swacch.dtos.DashBoardResponse;
import com.prudhvi.swacch.dtos.WasteCollectionRequest;
import com.prudhvi.swacch.dtos.WasteCollectionResponse;
import com.prudhvi.swacch.service.WasteService;

@RestController
public class WasteController {
	
	private WasteService service;
	
	WasteController(WasteService service){
		this.service=service;
	}
	
	@PostMapping("/collector/save")
	public WasteCollectionResponse save(@RequestBody WasteCollectionRequest waste,Authentication auth) throws BadRequestException {
		return service.save(waste,auth);
	}
	
	@GetMapping("/admin/dashboard")
	private DashBoardResponse dashboard() {
		return service.dashboard();
	}
	
	@GetMapping("/history")
	private Page<WasteCollectionResponse> getWasteByCollectorIdAndCurrentDate(Authentication auth,@RequestParam(defaultValue = "0") 
	int page,@RequestParam(defaultValue = "10")int size,@RequestParam(required = false) String status ,
	@RequestParam(required=false) String date) throws BadRequestException{
		if(auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
			return service.getAllCollections(auth,page,size,status,date);
		}else if(auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_COLLECTOR"))){
			return service.getWasteByCollectorIdAndCurrentDate(auth, page, size, status, date);
		}
		throw new BadRequestException("Role is neither an ADMIN nor a COLLECTOR!");
	}
}
