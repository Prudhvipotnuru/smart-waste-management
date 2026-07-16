package com.prudhvi.swacch.jwt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.prudhvi.swacch.dtos.AppUserDetails;
import com.prudhvi.swacch.model.User;
import com.prudhvi.swacch.repos.UserRepo;
import com.prudhvi.swacch.service.AppUserDetailsService;

@RestController
public class AuthController {

    private final AuthenticationManager authManager;
    private final JwtService jwtService;
    private final AppUserDetailsService appUserDetailsService;
    private final UserRepo urepo;
    
    private static final Logger log=LoggerFactory.getLogger(AuthController.class);

    public AuthController(AuthenticationManager authManager,
                          JwtService jwtService,
                          AppUserDetailsService appUserDetailsService,UserRepo urepo) {
        this.authManager = authManager;
        this.jwtService = jwtService;
        this.appUserDetailsService = appUserDetailsService;
		this.urepo = urepo;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {

        authManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        ); 
        User user = urepo.findByEmailOrName(request.getUsername(),request.getUsername());

        AppUserDetails userDetails =
        		appUserDetailsService.loadUserByUsername(request.getUsername());

        String token = jwtService.generateToken(userDetails,user);
        
        log.info(token);

        return ResponseEntity.ok(new AuthResponse(token, !user.isPasswordChanged()));
    }
}
