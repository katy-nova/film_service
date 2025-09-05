package com.example.films;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import com.example.films.dto.jwt.JwtAuthenticationDto;
import com.example.films.security.CustomUserDetails;
import com.example.films.security.jwt.JwtService;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class JwtServiceTest {

    @Autowired
    private JwtService jwtService;

    @Mock
    private CustomUserDetails customUserDetails;

    private final String username = "testuser";

    @Value("${JWT_SECRET}")
    private String jwtSecret;

    @BeforeEach
    void setUp() {
        when(customUserDetails.getUsername()).thenReturn(username);
    }

    @Test
    void generateAuthenticationToken_validUserDetails_returnsDtoWithTokens() {
        JwtAuthenticationDto jwtAuthenticationDto = jwtService.generateAuthenticationToken(customUserDetails);
        assertThat(jwtAuthenticationDto).isNotNull();
        assertThat(jwtAuthenticationDto.getToken()).isNotBlank();
        assertThat(jwtAuthenticationDto.getRefreshToken()).isNotBlank();
    }

    @Test
    void validateJwtToken_validToken_returnsTrue() {
        String token = jwtService.generateAuthenticationToken(customUserDetails).getToken();
        boolean isValid = jwtService.validateJwtToken(token);
        assertTrue(isValid);
    }

    @Test
    void validateJwtToken_expiredToken_returnsFalse() {
        Date expirationDate = Date.from(LocalDateTime.now().minusMinutes(1).atZone(ZoneId.systemDefault()).toInstant());
        String expiredToken = Jwts.builder()
                .subject(username)
                .expiration(expirationDate)
                .signWith(Keys.hmacShaKeyFor(java.util.Base64.getDecoder().decode(jwtSecret)))
                .compact();
        boolean isValid = jwtService.validateJwtToken(expiredToken);
        assertFalse(isValid);
    }

    @Test
    void validateJwtToken_invalidSignatureToken_returnsFalse() {
        String validToken = jwtService.generateAuthenticationToken(customUserDetails).getToken();
        String[] tokenParts = validToken.split("\\.");
        String tamperedToken = tokenParts[0] + "." + tokenParts[1] + ".fakedsignature"; // Alter the signature
        boolean isValid = jwtService.validateJwtToken(tamperedToken);
        assertFalse(isValid);
    }

    @Test
    void validateJwtToken_nullToken_returnsFalse() {
        boolean isValid = jwtService.validateJwtToken(null);
        assertFalse(isValid);
    }

    @Test
    void getLoginFromToken_validToken_returnsUsername() {
        String token = jwtService.generateAuthenticationToken(customUserDetails).getToken();
        String login = jwtService.getLoginFromToken(token);
        assertEquals(username, login);
    }

    @Test
    void getLoginFromToken_invalidToken_returnsNull() {
        String invalidToken = "invalid.token";
        assertThrows(MalformedJwtException.class, () -> jwtService.getLoginFromToken(invalidToken));
    }

    @Test
    void refreshBaseToken_validUserDetailsAndRefreshToken_returnsDtoWithNewTokenAndSameRefreshToken() {
        JwtAuthenticationDto initialDto = jwtService.generateAuthenticationToken(customUserDetails);
        String refreshToken = initialDto.getRefreshToken();
        JwtAuthenticationDto refreshedDto = jwtService.refreshBaseToken(customUserDetails, refreshToken);
        assertThat(refreshedDto).isNotNull();
        assertThat(refreshedDto.getToken()).isNotBlank();
        assertThat(refreshedDto.getRefreshToken()).isEqualTo(refreshToken);
    }
}
