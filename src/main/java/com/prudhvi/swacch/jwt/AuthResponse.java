package com.prudhvi.swacch.jwt;

import lombok.Data;

@Data
public class AuthResponse {
    private String token;
    
    private boolean mustChangePassword;

    public AuthResponse(String token, boolean mustChangePassword) {
        this.token = token;
        this.mustChangePassword = mustChangePassword;
    }
}
