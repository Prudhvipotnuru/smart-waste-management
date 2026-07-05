package com.prudhvi.swacch.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.prudhvi.swacch.model.User;
import com.prudhvi.swacch.model.UserRole;
import com.prudhvi.swacch.repos.UserRepo;
import com.prudhvi.swacch.service.NotificationService;
import com.prudhvi.swacch.utils.UploadUtil;

import jakarta.servlet.http.HttpServletResponse;

@RestController
public class UserController {
	
	private JobOperator jobOperator;

	private Job collectorImportJob;
	
	private NotificationService notificationService;
	
	private UserRepo uRepo;
	
	public UserController(JobOperator jobOperator,@Qualifier("collectorImportJob") Job collectorImportJob,
			NotificationService service, UserRepo repo) {
		this.jobOperator = jobOperator;
		this.collectorImportJob = collectorImportJob;
		this.notificationService = service;
		this.uRepo = repo;
	}

	@PostMapping("/admin/colUpload")
	public ResponseEntity<Map<String, String>> uploadFile(@RequestParam("file") MultipartFile file, HttpServletResponse response) {
	    ResponseEntity<Map<String, String>> status = UploadUtil.uploadProcess(file, jobOperator, collectorImportJob, 60);
	    if(status.getBody().get("status")=="SUCCESS" && status.getBody().get("jobExecution")==BatchStatus.COMPLETED.name()) {
	    	// Simple approach: get all collectors created in this run
	        List<User> newCollectors = uRepo.findByRoleAndPasswordChangedFalse(UserRole.COLLECTOR);

	        for (User c : newCollectors) {
	            // tempPassword is transient; you may need to pass it via a map or error file
	            notificationService.sendCollectorCredentials(
	                    c.getEmail(),
	                    c.getName(),
	                    "password"
	            );
	        }
	    }
	    return status;
	}
}
