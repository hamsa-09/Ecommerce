package com.example.vertexspace_server.controller;

import com.example.vertexspace_server.dto.ErrorResponse;
import com.example.vertexspace_server.dto.JwtResponse;
import com.example.vertexspace_server.dto.LoginRequest;
import com.example.vertexspace_server.dto.RegisterRequest;
import com.example.vertexspace_server.dto.SuccessResponse;
import com.example.vertexspace_server.exception.InvalidCredentialsException;
import com.example.vertexspace_server.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        return authService.logout();
    }

    @GetMapping("/department")
    public ResponseEntity<?> getDepartment() {
        return authService.getDepartment();
    }

}
