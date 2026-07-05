package com.prudhvi.swacch.utils;

import java.io.File;
import java.util.Map;

import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

public class UploadUtil {
	public static ResponseEntity<Map<String, String>> uploadProcess(MultipartFile file,JobOperator jobOperator,
			Job job,long headerLength) {
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

	        JobExecution jobExecution = jobOperator.start(job, params);
	        System.out.println(jobExecution.toString() +" Job status = " + jobExecution.getStatus());
	        
	        File errorFile = new File(uploadDir + "/error_records.csv");
	        if (errorFile.exists() && errorFile.length() > headerLength) {
	            return ResponseEntity.ok(
	                    Map.of(
	                            "status", "COMPLETED_WITH_ERRORS",
	                            "downloadUrl", "/download-errors"
	                    )
	            );
	        }

	        return ResponseEntity.ok(
	                Map.of("status", "SUCCESS","jobExecution",jobExecution.getStatus().name())
	        );
	    } catch (Exception e) {
	        e.printStackTrace();
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	        		.body(Map.of("status", "FAILED", "error", e.getMessage()));
	    }
	}
}
