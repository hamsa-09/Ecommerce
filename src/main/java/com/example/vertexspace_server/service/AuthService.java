package com.example.vertexspace_server.service;

import com.example.vertexspace_server.dto.JwtResponse;
import com.example.vertexspace_server.dto.LoginRequest;
import com.example.vertexspace_server.dto.RegisterRequest;
import com.example.vertexspace_server.dto.SuccessResponse;
import org.springframework.http.ResponseEntity;

public interface AuthService {
    ResponseEntity<?> register(RegisterRequest request);
    ResponseEntity<SuccessResponse<JwtResponse>> login(LoginRequest request);
    ResponseEntity<SuccessResponse<String>> logout();
    ResponseEntity<?> getDepartment();
}
