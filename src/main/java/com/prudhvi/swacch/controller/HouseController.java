package com.prudhvi.swacch.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.prudhvi.swacch.dtos.HouseRequest;
import com.prudhvi.swacch.dtos.HouseResponse;
import com.prudhvi.swacch.service.HouseService;
import com.prudhvi.swacch.utils.UploadUtil;

import jakarta.servlet.http.HttpServletResponse;

@RestController
public class HouseController {

	private HouseService service;

	private JobOperator jobOperator;

	private Job houseImportjob;
	
	public HouseController(HouseService service, JobOperator jobOperator,@Qualifier("houseImportJob") Job houseImportjob) {
		this.service = service;
		this.houseImportjob = houseImportjob;
		this.jobOperator = jobOperator;
	}

	@PostMapping("/admin/upload")
	public ResponseEntity<Map<String, String>> uploadFile(@RequestParam("file") MultipartFile file, HttpServletResponse response) {
	    return UploadUtil.uploadProcess(file, jobOperator, houseImportjob, 59);
	}

	@GetMapping("/download-errors")
	public void downloadErrors(HttpServletResponse response) throws IOException {
	    String path = System.getProperty("user.dir") + "/uploads/error_records.csv";
	    File file = new File(path);

	    response.setContentType("text/csv");
	    response.setHeader("Content-Disposition", "attachment; filename=error_records.csv");

	    Files.copy(file.toPath(), response.getOutputStream());
	    response.getOutputStream().flush();
	}

	@PostMapping("/admin/saveHouse")
	public HouseResponse save(@RequestBody HouseRequest request) {
		return service.save(request);
	}

	@GetMapping("/admin/houses")
	private List<HouseResponse> getAllHouses() {
		return service.getAllHouses();
	}

	@GetMapping("/houses/{id}")
	private HouseResponse getHouse(@PathVariable Long id) {
		return service.getHouse(id);
	}

}
