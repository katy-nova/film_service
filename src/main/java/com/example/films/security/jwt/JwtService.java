package com.example.films.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.example.films.dto.jwt.JwtAuthenticationDto;
import com.example.films.security.CustomUserDetails;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Component
public class JwtService {

    private static final Logger LOGGER = LogManager.getLogger(JwtService.class);

    @Value("${JWT_SECRET}")
    private String jwtSecret;

    public JwtAuthenticationDto generateAuthenticationToken(CustomUserDetails customUserDetails) {
        JwtAuthenticationDto jwtAuthenticationDto = new JwtAuthenticationDto();
        jwtAuthenticationDto.setToken(generateJwtToken(customUserDetails));
        jwtAuthenticationDto.setRefreshToken(generateRefreshToken(customUserDetails));
        return jwtAuthenticationDto;
    }

    public boolean validateJwtToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSignInKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return true;
        } catch (ExpiredJwtException e) {
            LOGGER.error("Expired JWT token", e);
        } catch (UnsupportedJwtException e) {
            LOGGER.error("Unsupported JWT token", e);
        } catch (MalformedJwtException e) {
            LOGGER.error("Malformed JWT token", e);
        } catch (SecurityException e) {
            LOGGER.error("Security exception", e);
        } catch (Exception e) {
            LOGGER.error("Unexpected error", e);
        }
        return false;
    }

    public String getLoginFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.getSubject();
    }

    public JwtAuthenticationDto refreshBaseToken(CustomUserDetails customUserDetails, String refreshToken) {

        JwtAuthenticationDto jwtAuthenticationDto = new JwtAuthenticationDto();
        jwtAuthenticationDto.setToken(generateJwtToken(customUserDetails));
        jwtAuthenticationDto.setRefreshToken(refreshToken);
        return jwtAuthenticationDto;
    }

    private String generateJwtToken(CustomUserDetails customUserDetails) {
        Date date = Date.from(LocalDateTime.now().plusMinutes(15).atZone(ZoneId.systemDefault()).toInstant());
        return Jwts.builder()
                .subject(customUserDetails.getUsername())
                .expiration(date)
                .signWith(getSignInKey())
                .compact();
    }

    private String generateRefreshToken(CustomUserDetails customUserDetails) {
        Date date = Date.from(LocalDateTime.now().plusDays(1).atZone(ZoneId.systemDefault()).toInstant());
        return Jwts.builder()
                .subject(customUserDetails.getUsername())
                .expiration(date)
                .signWith(getSignInKey())
                .compact();
    }

    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
