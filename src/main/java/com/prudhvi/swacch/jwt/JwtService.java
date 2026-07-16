package com.prudhvi.swacch.jwt;

import java.security.Key;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Service;

import com.prudhvi.swacch.dtos.AppUserDetails;
import com.prudhvi.swacch.model.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    // must be BASE64 encoded and long enough
    private static final String SECRET_KEY =
            "prudhvi'ssmartwastemanagementsystem"; 

    private Key getSignKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }

    public String generateToken(AppUserDetails userDetails, User user) {

        return Jwts.builder()
                .subject(userDetails.getUsername())
                .claim("email",user.getEmail())
                .claim("userName",user.getName())
                .claim("userId",user.getId())
                .claim("roles", userDetails.getAuthorities())
                .claim("mustChangePassword", !user.isPasswordChanged())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 1000 * 600))
                .signWith(getSignKey())
                .compact();
    }

    public String extractUsername(String token) {
        return extractClaims(token).getSubject();
    }

    public boolean isValid(String token, AppUserDetails userDetails) {
        final String phone = extractUsername(token);
        return phone.equals(userDetails.getUsername()) && !isExpired(token);
    }

    private boolean isExpired(String token){
        return extractClaims(token).getExpiration().before(new Date());
    }

    public boolean mustChangePassword(String token) {
    	return (boolean) extractClaims(token).get("mustChangePassword");
    }
    
    private Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith((SecretKey) getSignKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}