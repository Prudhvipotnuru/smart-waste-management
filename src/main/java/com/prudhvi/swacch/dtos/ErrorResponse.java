package com.prudhvi.swacch.dtos;

import java.time.Instant;

import lombok.Data;

@Data
public class ErrorResponse {
	private int status;
	private String error;
	private String message;
	private String path;
	private Instant timeStamp=Instant.now();
}
