package com.example.films.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.films.dto.jwt.JwtAuthenticationDto;
import com.example.films.dto.jwt.RefreshTokenDto;
import com.example.films.dto.jwt.UserCredentialDto;
import com.example.films.service.AuthenticationService;

import javax.naming.AuthenticationException;

@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthenticationService authService;

    @PostMapping("/sign-in")
    public ResponseEntity<JwtAuthenticationDto> signIn(@RequestBody UserCredentialDto userCredential) throws AuthenticationException {
        try {
            JwtAuthenticationDto jwtAuthenticationDto = authService.signIn(userCredential);
            return ResponseEntity.ok(jwtAuthenticationDto);
        } catch (AuthenticationException e) {
            throw new AuthenticationException(e.getMessage());
        }
    }

    @PostMapping("/refresh")
    public JwtAuthenticationDto refresh(@RequestBody RefreshTokenDto refreshTokenDto) throws AuthenticationException {
        return authService.refreshToken(refreshTokenDto);
    }

}
