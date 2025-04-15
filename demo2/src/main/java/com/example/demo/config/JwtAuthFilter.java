package com.example.demo.config;

import com.example.demo.service.JWTService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    @Autowired
    private JWTService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        String token = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
            try {
                // doar validare simplă
                if (jwtService.validateToken(token)) {
                    System.out.println("Token valid în Microserviciul 2");
                    // aici poți extrage informații dacă ai nevoie:
                    String username = jwtService.extractUsername(token);
                    Long userId = jwtService.extractUserId(token);
                    System.out.println("Username extras: " + username + " | UserId extras: " + userId);
                } else {
                    System.out.println("Token invalid");
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token invalid");
                    return;
                }
            } catch (Exception e) {
                System.out.println("Eroare la validarea tokenului: " + e.getMessage());
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token invalid sau expirat");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
