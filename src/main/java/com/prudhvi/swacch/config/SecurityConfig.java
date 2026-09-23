package com.prudhvi.swacch.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.prudhvi.swacch.jwt.JwtAuthFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
	private final JwtAuthFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;
	public SecurityConfig(JwtAuthFilter jwtAuthFilter, UserDetailsService userDetailsService) {
		this.jwtAuthFilter=jwtAuthFilter;
		this.userDetailsService=userDetailsService;
	}
	
	@Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
        		.csrf(csrf -> csrf.disable())
                .sessionManagement(sess ->
                        sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                		.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/login.html",
                        				"/home.html",
                        				"/",
                        				"/check",
                        				"/error",
                        				"/user",
                        				"/login",
                        				"/css/**",
                                		"/js/**","/uploads/**",
                       				 	"/createHouse.html",
                       				 	"/dashboard.html",
                       				 	"/houseList.html",
                       				 	"/history.html",
                       				 	"/waste.html",
                       				 	"/change-password.html",
                       				 	"/uploadFile.html").permitAll()
                        //Role-based rules
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/collector/**"
                        				).hasRole("COLLECTOR")
                        .requestMatchers("/change-password","/history").hasAnyRole("ADMIN","COLLECTOR")

                        .anyRequest().authenticated()
                )
                .authenticationProvider(provider())
                .addFilterBefore(jwtAuthFilter,
                        UsernamePasswordAuthenticationFilter.class)
                .build();
    }
	
	@Bean
	public AuthenticationProvider provider() {
		DaoAuthenticationProvider dap=new DaoAuthenticationProvider(userDetailsService);
		dap.setPasswordEncoder(passwordEncoder());
		return dap;
	}
	
	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration config) {
		return config.getAuthenticationManager();
	}
	
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
	
	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
	    CorsConfiguration config = new CorsConfiguration();
	    config.setAllowedOrigins(List.of("http://localhost:5173"));
	    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
	    config.setAllowedHeaders(List.of("*"));
	    config.setAllowCredentials(true);
	    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
	    source.registerCorsConfiguration("/**", config);
	    return source;
	}
}
