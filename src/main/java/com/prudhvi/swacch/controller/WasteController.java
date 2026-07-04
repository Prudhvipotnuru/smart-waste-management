package com.prudhvi.swacch.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.prudhvi.swacch.dtos.DashBoardResponse;
import com.prudhvi.swacch.dtos.WasteCollectionRequest;
import com.prudhvi.swacch.dtos.WasteCollectionResponse;
import com.prudhvi.swacch.model.WasteCollection;
import com.prudhvi.swacch.service.WasteService;

@RestController
public class WasteController {
	
	private WasteService service;
	
	WasteController(WasteService service){
		this.service=service;
	}
	
	@PostMapping("/collector/save")
	public WasteCollectionResponse save(@RequestBody WasteCollectionRequest waste,Authentication auth) {
		return service.save(waste,auth);
	}
	
	@GetMapping("/waste")
	private List<WasteCollectionResponse> getAll(){
		return service.getAll();
	}
	
	@GetMapping("/waste/house/{id}")
	private List<WasteCollectionResponse> getWasteByHouseId(@PathVariable Long id) {
		return service.getWasteByHouseId(id);
	}
	
	@GetMapping("/waste/collector/{id}")
	private List<WasteCollectionResponse> getWasteByCollectorId(@PathVariable Long id) {
		return service.getWasteByCollectorId(id);
	}
	
	@GetMapping("/admin/dashboard")
	private DashBoardResponse dashboard() {
		return service.dashboard();
	}
	
	@GetMapping("/collector/history")
	private Page<WasteCollectionResponse> getWasteByCollectorIdAndCurrentDate(Authentication auth,@RequestParam(defaultValue = "0") 
	int page,@RequestParam(defaultValue = "10")int size,@RequestParam(required = false) String status  ){
		return service.getWasteByCollectorIdAndCurrentDate(auth, page, size, status);
	}
}
