package com.app.taskmanager.security;

import com.app.taskmanager.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class JwtProcessing {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    // - public API of the class -

    public String generateToken(User user) {
        return Jwts.builder()
                .subject(user.getUsername())                // who this token belongs to (subject)
                .claim("role", user.getRole().name())        // extra data you want to embed (claim)
                .issuedAt(new Date())                       // when it was created (issuedAt)
                .expiration(new Date(System.currentTimeMillis() + expiration))  // when the token expires (expiration)
                .signWith(getSigningKey())                  // sign it with the secret key (in application.properties)
                .compact();                                 // build the final string (the whole token)
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
        } catch (Exception ex) {
            return false;
        }
    }

    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }

    // - private helper methods -

    private boolean isTokenExpired(String token) {
        return parseClaims(token).getExpiration().before(new Date());
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }

}
