package com.prudhvi.swacch.jwt;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.prudhvi.swacch.dtos.AppUserDetails;
import com.prudhvi.swacch.service.AppUserDetailsService;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

	private final JwtService jwtService;
	private final AppUserDetailsService appUserDetailsService;

	public JwtAuthFilter(JwtService jwtService, AppUserDetailsService appUserDetailsService) {
		this.jwtService = jwtService;
		this.appUserDetailsService = appUserDetailsService;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		final String authHeader = request.getHeader("Authorization");

		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			filterChain.doFilter(request, response);
			return;
		}

		String token = authHeader.substring(7);
		String username = null;
		try {
			username = jwtService.extractUsername(token);

			if (jwtService.mustChangePassword(token) && !request.getRequestURI().contains("change-password")) {
				response.setStatus(HttpServletResponse.SC_FORBIDDEN);
				response.setContentType("application/json");
				response.getWriter().write("""
				    {
			            "message":"Password change required"
			        }
			        """);
				return;
			}

			if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

				AppUserDetails userDetails = appUserDetailsService.loadUserByUsername(username);

				if (jwtService.isValid(token, userDetails)) {

					UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails,
							null, userDetails.getAuthorities());

					authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

					SecurityContextHolder.getContext().setAuthentication(authToken);
				}
			}
		} catch (ExpiredJwtException e) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			response.getWriter().write("""
				    {
		            "message":"Session Expired Please Login Again..."
		        }
		        """);
			return;
		}

		filterChain.doFilter(request, response);
	}
}