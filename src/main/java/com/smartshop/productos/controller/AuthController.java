package com.smartshop.productos.controller;

import com.smartshop.productos.security.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController // Indica que esta clase es un controlador REST
@RequestMapping("/api/public/auth") // Ruta base para los endpoints de autenticación
@RequiredArgsConstructor // Genera automáticamente el constructor con los campos final
public class AuthController {

    private final JwtUtil jwtUtil; // Inyectamos JwtUtil para generar tokens

    // Endpoint para generar un token a partir del usuarioId
    @GetMapping("/token")
    public ResponseEntity<Map<String, String>> generarToken(@RequestParam Long usuarioId) {
        // Generamos el token usando JwtUtil
        String token = jwtUtil.generateToken(usuarioId);

        // Creamos una respuesta con el token dentro de un mapa
        Map<String, String> respuesta = new HashMap<>();
        respuesta.put("token", token);

        // Devolvemos el token como respuesta
        return ResponseEntity.ok(respuesta);
    }
}
