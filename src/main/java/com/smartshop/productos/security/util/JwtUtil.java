package com.smartshop.productos.security.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.function.Function;

@Component
public class JwtUtil {

    // Clave secreta usada para firmar y verificar los tokens JWT
    private static final String SECRET_KEY = "MI_SECRETA_CLAVE_DE_256_BITS_DE_EJEMPLO123456789012345678901234567890";

    // Devuelve la clave secreta convertida al tipo que necesita JJWT
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }

    // Extrae el nombre de usuario (sub = subject) desde el token
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Extrae la fecha de expiración del token
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // Extrae cualquier información (claim) del token usando una función
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // Devuelve todos los "claims" del token (información embebida)
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey()) // Usamos la clave secreta para validar el token
                .build()
                .parseClaimsJws(token)
                .getBody(); // Obtenemos el contenido del token (los claims)
    }

    // Verifica si el token ya expiró
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // Valida si el token es válido y corresponde al usuario esperado
    public boolean isTokenValid(String token, String username) {
        final String tokenUsername = extractUsername(token);
        return (tokenUsername.equals(username) && !isTokenExpired(token));
    }

    // Genera un nuevo token JWT dado un nombre de usuario
    public String generateToken(Long usuarioId) {
        return Jwts.builder()
                .setSubject(usuarioId.toString()) // "sub": lo usamos como nombre de usuario
                .setIssuedAt(new Date()) // Fecha de creación
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10)) // Expira en 10 horas
                .signWith(getSigningKey(), SignatureAlgorithm.HS256) // Firma con HMAC-SHA256
                .compact(); // Lo convierte en un string JWT
    }
}

