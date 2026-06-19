package com.stockservice.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        
        // Allows credential sharing (cookies, auth headers, etc.)
        config.setAllowCredentials(true);
        
        // This explicitly allows your frontend clients (like Vue/React or local ports) to talk to your backend
        config.setAllowedOrigins(Arrays.asList(
            "http://localhost:5173", 
            "http://localhost:5174",
            "http://127.0.0.1:5173"
        ));
        
        // Allows all headers and specific REST method actions
        config.setAllowedHeaders(Arrays.asList("*"));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        
        // Apply this configuration ruleset to all API paths
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}