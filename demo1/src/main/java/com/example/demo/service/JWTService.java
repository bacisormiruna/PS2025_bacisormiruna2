package com.example.demo.service;

import com.example.demo.dto.userdto.UserDTO;
import com.example.demo.entity.User;
import com.example.demo.errorhandler.UserException;
import io.jsonwebtoken.*;

import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JWTService {

    //private final UserService userService;
    private String key =""; //"m6AlbnFzbCYyYW6BIGdlbmVyYXRlZCBrZXkgZm9yIIIIc3Rpbmc?";
    private static final String SECRET_KEY = "m6AlbnFzbCYyYW6BIGdlbmVyYXRlZCBrZXkgZm9yIIIIc3Rpbmc?";


    public String generateToken(UserDTO userDTO) throws UserException {
        Map<String, Object> claims = new HashMap<>();
        claims.put("name", userDTO.getName());
        claims.put("id", userDTO.getId());  // Modificat de la "userId" la "id" pentru consistență
        claims.put("userRole", userDTO.getRoleName());
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userDTO.getName())
                .setId(userDTO.getId().toString())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10))
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }

    public String generatToken(String username) {
    return Jwts.builder()
            .setSubject(username)
            .setIssuedAt(new Date(System.currentTimeMillis()))
            .setExpiration(new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000)) // 24 ore
            .signWith(getKey())
            .compact();
}

    private SecretKey getKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Long extractUserId(String token) {
        try {
            JwtParser parser = Jwts.parserBuilder()
                    .setSigningKey(SECRET_KEY)
                    .build();
            return parser.parseClaimsJws(token)
                    .getBody()
                    .get("id", Long.class);
        } catch (JwtException e) {
            throw new IllegalArgumentException("Invalid token", e);
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


    public boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
}
