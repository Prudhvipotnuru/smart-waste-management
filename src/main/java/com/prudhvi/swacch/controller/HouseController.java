package com.prudhvi.swacch.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.http.HttpStatus;
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

import jakarta.servlet.http.HttpServletResponse;

@RestController
public class HouseController {

	private HouseService service;

	private JobLauncher joblauncher;

	private Job job;

	public HouseController(HouseService service, JobLauncher joblaucher, Job job) {
		this.service = service;
		this.job = job;
		this.joblauncher = joblaucher;
	}

	@PostMapping("/admin/upload")
	public ResponseEntity<Map<String, String>> uploadFile(@RequestParam("file") MultipartFile file, HttpServletResponse response) {
	    try {
	    	if (!file.getOriginalFilename().endsWith(".csv")) {
	    	    return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("status", "FAILED", "error", "Only CSV allowed"));
	    	}
	        // Absolute path (inside project folder or anywhere you like)
	        String uploadDir = System.getProperty("user.dir") + "/uploads";

	        // Create folder if it doesn't exist
	        File dir = new File(uploadDir);
	        if (!dir.exists()) {
	            dir.mkdirs(); // ✅ this creates uploads folder
	        }

	        // Save file
	        File dest = new File(dir, file.getOriginalFilename());
	        file.transferTo(dest);

	        System.out.println("Saved file to: " + dest.getAbsolutePath());

	        // Trigger Spring Batch job
	        JobParameters params = new JobParametersBuilder()
	                .addString("filePath", dest.getAbsolutePath())
	                .addLong("time", System.currentTimeMillis())
	                .toJobParameters();

	        joblauncher.run(job, params);
	        
	        File errorFile = new File(uploadDir + "/error_records.csv");
	        if (errorFile.exists() && errorFile.length() > 0) {
	            return ResponseEntity.ok(
	                    Map.of(
	                            "status", "COMPLETED_WITH_ERRORS",
	                            "downloadUrl", "/download-errors"
	                    )
	            );
	        }

	        return ResponseEntity.ok(
	                Map.of("status", "SUCCESS")
	        );
	    } catch (Exception e) {
	        e.printStackTrace();
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	        		.body(Map.of("status", "FAILED", "error", e.getMessage()));
	    }
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

	@PostMapping("/collector/saveHouse")
	private HouseResponse save(@RequestBody HouseRequest request) {
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
