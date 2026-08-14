package com.prudhvi.swacch.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.prudhvi.swacch.dtos.HouseRequest;
import com.prudhvi.swacch.dtos.HouseResponse;
import com.prudhvi.swacch.model.House;
import com.prudhvi.swacch.repos.HouseRepo;

import jakarta.servlet.http.HttpServletResponse;

@Service
public class HouseService {

	private HouseRepo repo;

	HouseService(HouseRepo repo) {
		this.repo = repo;
	}

	public HouseResponse save(HouseRequest request) {
		House house = new House();
		BeanUtils.copyProperties(request, house);
		house.setQrCodeValue("H:" + request.getHouseNumber());
		House resp = repo.save(house);
		HouseResponse response = new HouseResponse();
		BeanUtils.copyProperties(resp, response);
		return response;
	}

	public List<HouseResponse> getAllHouses(int page, int size) {
		PageRequest pr = PageRequest.of(page-1, size);
		Page<House> houses = repo.findAll(pr);
		List<HouseResponse> response = new ArrayList<HouseResponse>();
		for (House house : houses) {
			HouseResponse resp = new HouseResponse();
			BeanUtils.copyProperties(house, resp);
			response.add(resp);
		}
		return response;
	}

	public HouseResponse getHouse(Long id) {
		Optional<House> house = repo.findById(id);
		HouseResponse resp = new HouseResponse();
		if (house.isPresent()) {
			BeanUtils.copyProperties(house.get(), resp);
		}
		return resp;
	}

	public ResponseEntity<?> downloadErrors(HttpServletResponse response) {
		String path = System.getProperty("user.dir") + "/uploads/error_records.csv";
		File file = new File(path);

		response.setContentType("text/csv");
		response.setHeader("Content-Disposition", "attachment; filename=error_records.csv");

		try {
			Files.copy(file.toPath(), response.getOutputStream());
			response.getOutputStream().flush();
		} catch (IOException e) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Failed to download Error File", e);
		}
		return ResponseEntity.ok(200);
	}

}
