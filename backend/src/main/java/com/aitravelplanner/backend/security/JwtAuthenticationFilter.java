package com.aitravelplanner.backend.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        logger.info("Processing authentication for request: {}", request.getRequestURI());
        
        final String authorizationHeader = request.getHeader("Authorization");
        logger.info("Authorization header: {}", authorizationHeader != null ? "Bearer [hidden]" : "missing");

        String username = null;
        String jwt = null;

        // 从Authorization头中提取JWT token
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            logger.info("JWT token extracted: {}", jwt != null ? "[present]" : "null");
            
            try {
                username = jwtUtil.extractUsername(jwt);
                logger.info("Username extracted from JWT: {}", username);
            } catch (Exception e) {
                logger.error("Failed to extract username from JWT: {}", e.getMessage());
                logger.error("JWT validation error:", e);
            }
        } else {
            logger.info("No valid Authorization header found");
        }

        // 如果找到用户名并且当前没有认证信息
        if (username != null) {
            logger.info("Username found: {}, checking authentication status", username);
            
            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                logger.info("No existing authentication, proceeding with authentication");
                
                try {
                    UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
                    logger.info("User details loaded successfully for: {}", username);

                    // 验证token是否有效
                    if (jwtUtil.validateToken(jwt, userDetails)) {
                        logger.info("JWT token is valid for user: {}", username);
                        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                        usernamePasswordAuthenticationToken
                                .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
                        logger.info("User {} successfully authenticated", username);
                    } else {
                        logger.error("JWT token validation failed for user: {}", username);
                    }
                } catch (Exception e) {
                    logger.error("Error during user authentication: {}", e.getMessage());
                    logger.error("Authentication error:", e);
                }
            } else {
                logger.info("User already authenticated, skipping");
            }
        }
        
        logger.info("Authentication processing completed for: {}", request.getRequestURI());
        filterChain.doFilter(request, response);
    }
}