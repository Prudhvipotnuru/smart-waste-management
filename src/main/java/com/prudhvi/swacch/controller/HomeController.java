package com.prudhvi.swacch.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
	@GetMapping("/")
	public String home() {
		return "home";
	}
	
	@GetMapping("/check")
	public ResponseEntity<?> check(){
		return ResponseEntity.ok("Token is still not expired yet");
	}
	
}