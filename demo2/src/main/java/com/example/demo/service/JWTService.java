package com.example.demo.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;

@Service
public class JWTService {
    private static final String SECRET_KEY = "m6AlbnFzbCYyYW6BIGdlbmVyYXRlZCBrZXkgZm9yIIIIc3Rpbmc?";

    public boolean validateToken(String token) {
        return (!isTokenExpired(token));
    }
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
    private <T> T extractClaim(String token, Function<Claims, T> claimResolver){
        final Claims claims = extractAllClaims(token);
        return claimResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .setSigningKey(SECRET_KEY)  // Secretul folosit pentru semnarea token-ului
                .parseClaimsJws(token)  // Parsează token-ul
                .getBody();
    }

    public String extractUsername(String token) {
        System.out.println("Extracting username for token: {}"+ token);
        return Jwts.parser()
                .setSigningKey(getKey())
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public Long extractUserId(String token) {
        try {
            System.out.println("Extracting user id for token: {}"+ token);
            return Jwts.parser()
                    .setSigningKey(getKey())
                    .parseClaimsJws(token)
                    .getBody()
                    .get("id", Long.class);
        } catch (Exception e) {
            System.out.println("Error extracting user id"+ e);
            throw new IllegalArgumentException("Invalid token or userId not found", e);
        }
    }

    public String extractRoleName(String token) {
        try {
            JwtParser parser = Jwts.parserBuilder()
                    .setSigningKey(SECRET_KEY) // cheia ta secretă
                    .build();

            return parser.parseClaimsJws(token)
                    .getBody()
                    .get("userRole", String.class); // extragere corectă ca String
        } catch (JwtException e) {
            throw new IllegalArgumentException("Invalid token", e);
        }
    }

    private SecretKey getKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }

}
