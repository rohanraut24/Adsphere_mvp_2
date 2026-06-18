package com.adsphere.config;

import com.adsphere.security.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.*;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**", "/h2-console/**",
                        "/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html",
                        "/api/track/**").permitAll()
                .requestMatchers("/api/admin/**").hasRole("SUPER_ADMIN")
                .requestMatchers("/api/network/**").hasAnyRole("SUPER_ADMIN", "NETWORK_ADMIN")
                .requestMatchers("/api/publisher/upgrade-requests", "/api/publisher/upgrade-requests/**").hasAnyRole("SUPER_ADMIN", "NETWORK_ADMIN", "PUBLISHER", "ADVERTISER")
                .requestMatchers("/api/publisher/**").hasAnyRole("SUPER_ADMIN", "NETWORK_ADMIN", "PUBLISHER")
                .requestMatchers("/api/advertiser/**").hasAnyRole("SUPER_ADMIN", "NETWORK_ADMIN", "ADVERTISER")
                .anyRequest().authenticated()
            )
            .headers(h -> h.frameOptions(f -> f.disable())) // needed for H2 console
            .exceptionHandling(e -> e.authenticationEntryPoint((request, response, authException) -> {
                response.sendError(jakarta.servlet.http.HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
            }))
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return new ProviderManager(provider);
    }
}
