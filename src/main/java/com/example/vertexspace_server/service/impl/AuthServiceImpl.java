package com.example.vertexspace_server.service.impl;

import com.example.vertexspace_server.dto.ErrorResponse;
import com.example.vertexspace_server.dto.JwtResponse;
import com.example.vertexspace_server.dto.LoginRequest;
import com.example.vertexspace_server.dto.RegisterRequest;
import com.example.vertexspace_server.dto.SuccessResponse;
import com.example.vertexspace_server.exception.InvalidCredentialsException;
import com.example.vertexspace_server.model.Department;
import com.example.vertexspace_server.model.Role;
import com.example.vertexspace_server.model.UserAccount;
import com.example.vertexspace_server.repository.DepartmentRepository;
import com.example.vertexspace_server.repository.RoleRepository;
import com.example.vertexspace_server.repository.UserAccountRepository;
import com.example.vertexspace_server.security.JwtUtil;
import com.example.vertexspace_server.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.security.core.AuthenticationException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class AuthServiceImpl implements AuthService {
    @Autowired
    private UserAccountRepository userAccountRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private DepartmentRepository departmentRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public ResponseEntity<?> register(RegisterRequest request) {
        if (userAccountRepository.findByUsername(request.getUsername()) != null) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Username already exists"));
        }
        if (userAccountRepository.findByEmail(request.getEmail()) != null) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Email already exists"));
        }
        Role role = roleRepository.findByName(request.getRoleName());
        Department dept = departmentRepository.findAll().stream()
                .filter(d -> d.getName().equals(request.getDepartmentName()))
                .findFirst().orElse(null);
        if (role == null || dept == null) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Invalid role or department"));
        }
        UserAccount user = new UserAccount();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setRole(role);
        user.setDepartment(dept);
        userAccountRepository.save(user);
        return ResponseEntity.status(201).body(new SuccessResponse<>("User registered successfully"));
    }

    @Override
    public ResponseEntity<SuccessResponse<JwtResponse>> login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            UserAccount user = userAccountRepository.findByUsername(request.getUsername());
            List<String> roles = Collections.singletonList(user.getRole().getName());
            Map<String, Object> permissions = Collections.emptyMap();
            String token = jwtUtil.generateToken(user.getUsername(), roles, permissions);
            long expiresAt = System.currentTimeMillis() + 3600000;
            JwtResponse jwtResponse = new JwtResponse(token, user.getUsername(), roles, expiresAt);
            return ResponseEntity.ok(new SuccessResponse<>(jwtResponse));
        } catch (AuthenticationException ex) {
            throw new InvalidCredentialsException("Invalid username or password");
        }
    }

    @Override
    public ResponseEntity<SuccessResponse<String>> logout() {
        return ResponseEntity.ok(new SuccessResponse<>("Logged out successfully"));
    }

    @Override
    public ResponseEntity<SuccessResponse<List<String>>> getDepartment() {
        return ResponseEntity.ok(new SuccessResponse<>(departmentRepository.findAll().stream().map(Department::getName).toList()));
    }
}
