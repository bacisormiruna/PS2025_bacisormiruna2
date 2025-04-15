package com.example.demo.config;

import com.example.demo.service.JWTService;
import com.example.demo.service.MyUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;


@Component
public class JwtFilter extends OncePerRequestFilter{

    @Autowired
    private JWTService jwtService;
    @Autowired
    private ApplicationContext context;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        String token = null;
        String username = null;
        Long userId = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
            username = jwtService.extractUsername(token);
            userId = jwtService.extractUserId(token);
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null && userId != null) {

            UserDetails userDetails = null;

            try {
                userDetails = context.getBean(MyUserDetailsService.class).loadUserByUsername(username);
            } catch (UsernameNotFoundException e) {
                try {
                    userDetails = context.getBean(MyUserDetailsService.class).loadUserByUserId(userId);
                } catch (UsernameNotFoundException ex) {
                    // Dacă nici așa nu găsești utilizatorul, oprește procesul
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User not found");
                    return;
                }
            }

            if (jwtService.validateToken(token, userDetails)) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        filterChain.doFilter(request, response);
    }


//    @Override
//    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
//            throws ServletException, IOException {
//
//        final String authHeader = request.getHeader("Authorization");
//
//        // 1. Verifică prezența header-ului Authorization
//        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
//            logger.warn("Missing or invalid Authorization header");
//            filterChain.doFilter(request, response);
//            return;
//        }
//
//        try {
//            // 2. Extrage token-ul
//            final String token = authHeader.substring(7);
//
//            // 3. Extrage claim-urile
//            final String username = jwtService.extractUsername(token);
//            final Long userId = jwtService.extractUserId(token);
//
//            logger.debug("Extracted username: {}, userId: {}", username, userId);
//
//            // 4. Validare claim-uri
//            if (username == null || userId == null) {
//                logger.error("Token missing required claims");
//                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token claims");
//                return;
//            }
//
//            // 5. Verifică dacă autentificarea este deja setată
//            if (SecurityContextHolder.getContext().getAuthentication() == null) {
//
//                // 6. Încarcă UserDetails
//                UserDetails userDetails = context.getBean(MyUserDetailsService.class)
//                        .loadUserByUsername(username);
//
//                // 7. Validează token-ul
//                if (jwtService.validateToken(token, userDetails)) {
//                    logger.debug("Token validated successfully for user: {}", username);
//
//                    // 8. Creează obiectul de autentificare
//                    UsernamePasswordAuthenticationToken authToken =
//                            new UsernamePasswordAuthenticationToken(
//                                    userDetails,
//                                    null,
//                                    userDetails.getAuthorities()
//                            );
//                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
//
//                    // 9. Setează autentificarea în context
//                    SecurityContextHolder.getContext().setAuthentication(authToken);
//
//                    // 10. Adaugă userId ca atribut al request-ului
//                    request.setAttribute("userId", userId);
//                } else {
//                    logger.warn("Token validation failed");
//                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
//                    return;
//                }
//            }
//        } catch (Exception e) {
//            logger.error("Authentication error: {}", e.getMessage());
//            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentication failed");
//            return;
//        }
//
//        filterChain.doFilter(request, response);
//    }


}
