package com.smartshop.productos.security;

import com.smartshop.productos.security.annotations.RequiresAuth;
import com.smartshop.productos.security.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.io.IOException;
import java.util.List;

// Este filtro se ejecuta una vez por cada request HTTP
@Component
@RequiredArgsConstructor
public class    JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    //private final HandlerMapping handlerMapping;
    private final RequestMappingHandlerMapping handlerMapping;


    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {


        HandlerExecutionChain handler;
        try {
            handler = handlerMapping.getHandler(request);
        } catch (Exception e) {
            filterChain.doFilter(request, response);
            return;
        }

        if (handler == null || !(handler.getHandler() instanceof HandlerMethod handlerMethod)) {
            filterChain.doFilter(request, response);
            return;
        }

        boolean requiresAuth =
                handlerMethod.hasMethodAnnotation(RequiresAuth.class) ||
                        handlerMethod.getBeanType().isAnnotationPresent(RequiresAuth.class);

        if (!requiresAuth) {
            filterChain.doFilter(request, response);
            return;
        }



        // Obtener el valor del header "Authorization"
        final String authHeader = request.getHeader("Authorization");

        String jwt = null;
        String username = null;

        // Verificamos si el header contiene un token con el prefijo "Bearer "
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            jwt = authHeader.substring(7); // Quitamos "Bearer " y dejamos solo el token
            username = jwtUtil.extractUsername(jwt); // Extraemos el nombre de usuario desde el token
        }

        // Si hay un nombre de usuario y aún no hay autenticación activa
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // En un sistema real, buscaríamos al usuario en DB. Aquí lo simulamos.
            UserDetails userDetails = org.springframework.security.core.userdetails.User
                    .withUsername(username)
                    .password("") // No necesitamos la contraseña aquí
                    .authorities("USER") // Puedes cambiar roles
                    .build();

            // Validamos que el token corresponda al usuario y esté vigente
            if (jwtUtil.isTokenValid(jwt, username)) {

                // Creamos el objeto de autenticación (con usuario y roles)
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                // Detalles adicionales desde la request HTTP
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Guardamos la autenticación en el contexto de seguridad de Spring
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // Continuamos con la ejecución de los demás filtros o endpoint
        filterChain.doFilter(request, response);
    }
}

