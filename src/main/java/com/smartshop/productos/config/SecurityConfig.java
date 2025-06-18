package com.smartshop.productos.config;

import com.smartshop.productos.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // Deshabilita CSRF ya que es una API stateless
        http.csrf(csrf -> csrf.disable())

                // No se guardará estado de sesión
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Configura qué rutas están permitidas sin autenticación
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/public/**").permitAll()
                        //.requestMatchers("/api/v1/productos", "/api/v1/productos/**").permitAll()
                        .requestMatchers(
                                "/v3/api-docs/**", // Swagger JSON
                                "/swagger-ui/**",  // Recursos de Swagger
                                "/swagger-ui.html" // Página principal de Swagger
                        ).permitAll()
                        // Endpoints públicos
                        .anyRequest().authenticated() // Todo lo demás requiere autenticación
                )

                // Agrega el filtro personalizado antes del filtro estándar de autenticación
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // Exponer AuthenticationManager como bean
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}

