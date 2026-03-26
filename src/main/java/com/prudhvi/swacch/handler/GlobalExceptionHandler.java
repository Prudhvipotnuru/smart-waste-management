package com.prudhvi.swacch.handler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.prudhvi.swacch.dtos.ErrorResponse;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {
	@ExceptionHandler(EntityNotFoundException.class)
	public ResponseEntity<ErrorResponse> handle(EntityNotFoundException ex, HttpServletRequest request){
		ErrorResponse err = new ErrorResponse();
		err.setError("NOT_FOUND");
		err.setMessage(ex.getMessage());
		err.setStatus(HttpStatus.NOT_FOUND.value());
		err.setPath(request.getRequestURL().toString());
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(err);
	}
}
