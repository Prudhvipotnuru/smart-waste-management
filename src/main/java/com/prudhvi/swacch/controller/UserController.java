package com.prudhvi.swacch.controller;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.prudhvi.swacch.dtos.ChangePasswordRequest;
import com.prudhvi.swacch.model.CollectorCredential;
import com.prudhvi.swacch.model.User;
import com.prudhvi.swacch.repos.CollectorCredentialRepo;
import com.prudhvi.swacch.repos.UserRepo;
import com.prudhvi.swacch.service.NotificationService;
import com.prudhvi.swacch.utils.UploadUtil;

import jakarta.servlet.http.HttpServletResponse;

@RestController
public class UserController {
	
	public static final Logger log = LoggerFactory.getLogger(UserController.class);
	
	private JobOperator jobOperator;

	private Job collectorImportJob;
	
	private NotificationService notificationService;
	
	private CollectorCredentialRepo cRepo;
	
	private UserRepo uRepo;
	
	private PasswordEncoder passwordEncoder;
	
	public UserController(JobOperator jobOperator,@Qualifier("collectorImportJob") Job collectorImportJob,
			NotificationService service, CollectorCredentialRepo cRepo, 
			UserRepo uRepo, PasswordEncoder passwordEncoder) {
		this.jobOperator = jobOperator;
		this.collectorImportJob = collectorImportJob;
		this.notificationService = service;
		this.cRepo = cRepo;
		this.uRepo = uRepo;
		this.passwordEncoder = passwordEncoder;
	}

	@PostMapping("/admin/colUpload")
	public ResponseEntity<Map<String, String>> uploadFile(@RequestParam("file") MultipartFile file, HttpServletResponse response) {
	    ResponseEntity<Map<String, String>> status = UploadUtil.uploadProcess(file, jobOperator, collectorImportJob, 60);
		Map<String, String> body = status.getBody();
	    if(body != null && BatchStatus.COMPLETED.name().equals(body.get("jobExecution"))) {
	    	Long jobExecutionId = Long.valueOf(body.get("jobExecutionId"));
	        List<CollectorCredential> newCollectors = cRepo.findByJobExecutionId(jobExecutionId);
	        if(CollectionUtils.isEmpty(newCollectors)) {
	        	return status;
	        }
	        ExecutorService executor = Executors.newFixedThreadPool(5);

	        long start=System.currentTimeMillis();
	        List<?> list = newCollectors.stream()
	        		.map(c -> executor.submit(() -> notificationService.sendCollectorCredentials(c.getEmail(), c.getName(), c.getPassword())))
	        		.toList();
	        int failures=0;
	        for(Object f:list) {
	        	try {
	                ((Future<?>) f).get(30, TimeUnit.SECONDS); // surfaces exceptions, bounds wait time
	            } catch (ExecutionException e) {
	                failures++;
	                log.error("Failed to send credentials email", e.getCause());
	            } catch (TimeoutException e) {
	                failures++;
	                log.error("Timed out sending credentials email", e);
	            } catch (InterruptedException e) {
	                Thread.currentThread().interrupt();
	                log.error("Interrupted while sending emails", e);
	                break;
	            }
	        }
	        long end = System.currentTimeMillis();
	        log.info("{} ms total time to send mails to {} collectors ({} failures)",
	                (end - start), newCollectors.size(), failures);
	        
	        executor.shutdown();
	    }
	    cRepo.deleteAll();
	    return status;
	}
	
	@PostMapping("/change-password")
	public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest req) {

        User user = uRepo.findById(req.getUserId()).orElseThrow();

        if (!req.getNewPassword().equals(req.getConfirmPassword())) {
            return ResponseEntity.badRequest().body("Passwords did not match");
        }

        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        user.setPasswordChanged(true);

        uRepo.save(user);

        return ResponseEntity.ok("Password updated");
    }
}
