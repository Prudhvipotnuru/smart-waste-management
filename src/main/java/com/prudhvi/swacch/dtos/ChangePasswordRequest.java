package com.prudhvi.swacch.dtos;

import lombok.Data;

@Data
public class ChangePasswordRequest {
	private Long userId;
	private String newPassword;
	private String confirmPassword;
}
